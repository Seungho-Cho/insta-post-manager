package com.plamason.postmanager.service;

import com.plamason.postmanager.enums.AppSettingName;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudflareR2ServiceImpl implements ImageHostingService {

    private final AppSettingService appSettingService;

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final int THUMB_WIDTH = 300;
    private static final int THUMB_HEIGHT = 300;

    private static final String BASE_DIR = "post/";
    private static final String THUMB_DIR = "thumb/";

    // 지원하는 이미지 포맷과 MIME 타입 정의
    private static final Map<String, String> SUPPORTED_FORMATS = Map.of(
            "jpg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp"
    );

    String endpoint;
    String accessKey;
    String secretKey;
    String bucket;
    String openUrl;

    @PostConstruct
    public void init() {
        Map<String, String> appSettings = appSettingService.getAllSettings();
        endpoint = appSettings.get(AppSettingName.R2_ENDPOINT_URL.toString());
        accessKey = appSettings.get(AppSettingName.R2_ACCESS_TOKEN.toString());
        secretKey = appSettings.get(AppSettingName.R2_API_KEY.toString());
        bucket = appSettings.get(AppSettingName.R2_BUCKET_NAME.toString());
        openUrl = appSettings.get(AppSettingName.R2_OPEN_URL.toString());


        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("R2_ENDPOINT_URL is not configured");
        }
        if (accessKey == null || accessKey.isBlank()) {
            throw new IllegalStateException("R2_ACCESS_TOKEN is not configured");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("R2_API_KEY is not configured");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("R2_BUCKET_NAME is not configured");
        }
        if (openUrl == null || openUrl.isBlank()) {
            throw new IllegalStateException("R2_OPEN_URL is not configured");
        }
    }

    @Override
    public String uploadImage(String imageUrl, String saveName) {
        String result = null;
        try (InputStream in = new URL(imageUrl).openStream();
             S3Client r2Client = buildR2Client(endpoint, accessKey, secretKey)) {

            BufferedImage original = ImageIO.read(in);
            if (original == null) throw new IOException("이미지 로드 실패");

            // 이미지 확장자 추출
            String format = extractFormatFromImage(imageUrl);
            if (!SUPPORTED_FORMATS.containsKey(format)) {
                throw new IllegalArgumentException("지원하지 않는 이미지 포맷입니다: " + format);
            }

            // 인스타그램 비율에 맞도록 레터박스 추가
            BufferedImage adjustedImage = ensureInstagramRatio(original);

            // 고화질 리사이즈 처리
            BufferedImage resized = resizeImage(adjustedImage, MAX_WIDTH, MAX_HEIGHT);

            // 원본 이미지 업로드 (JPEG 품질 90% 설정)
            result = uploadToR2WithQuality(r2Client, bucket, BASE_DIR + saveName + "." + format, resized, format, 0.9f);

            // 썸네일 이미지 업로드 (작은 크기 유지)
            BufferedImage thumb = resizeImage(resized, THUMB_WIDTH, THUMB_HEIGHT);
            uploadToR2WithQuality(r2Client, bucket, THUMB_DIR + saveName + "." + format, thumb, format, 0.9f);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 고화질 리사이즈 처리를 위한 메서드
     */
    private BufferedImage resizeImage(BufferedImage image, int maxWidth, int maxHeight) throws IOException {
        if (image.getWidth() <= maxWidth && image.getHeight() <= maxHeight) {
            // 리사이즈가 필요 없는 경우 원본 반환
            return image;
        }

        return Thumbnails.of(image)
                .size(maxWidth, maxHeight)
                .outputQuality(1.0f) // 화질 유지 (최대 품질)
                .asBufferedImage();
    }

    /**
     * R2 업로드 시 이미지 품질을 설정하여 업로드
     */
    private String uploadToR2WithQuality(S3Client client, String bucket, String key, BufferedImage image,
                                         String format, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // JPEG 품질 설정
        if ("jpg".equals(format) || "jpeg".equals(format)) {
            ImageIO.write(getCompressedJPEGImage(image, quality), "jpg", baos);
        } else {
            // 기타 포맷(png, webp 등)은 기본 처리
            ImageIO.write(image, format, baos);
        }

        byte[] bytes = baos.toByteArray();
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(SUPPORTED_FORMATS.get(format))
                .contentLength((long) bytes.length)
                .build();

        client.putObject(req, RequestBody.fromBytes(bytes));
        return openUrl + "/" + key; // 공개 URL 반환
    }

    /**
     * JPEG 형식의 이미지에 압축을 적용
     */
    private BufferedImage getCompressedJPEGImage(BufferedImage image, float quality) throws IOException {
        // JPEG 압축 처리
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose(); // 리소스 해제
        }

        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * 인스타그램 지원 비율(4:5 ~ 1.91:1)에 맞춰 레터박스를 추가
     */
    private BufferedImage ensureInstagramRatio(BufferedImage original) {
        final double MAX_RATIO = 1.91; // 최대 가로비 (1.91:1)
        final double MIN_RATIO = 0.8;  // 최대 세로비 (4:5)

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        double currentRatio = (double) originalWidth / originalHeight;

        // 비율이 이미 알맞다면 원본 반환
        if (currentRatio <= MAX_RATIO && currentRatio >= MIN_RATIO) {
            return original;
        }

        int targetWidth, targetHeight;
        if (currentRatio > MAX_RATIO) {
            // 가로가 지나치게 긴 경우
            targetWidth = originalWidth;
            targetHeight = (int) (originalWidth / MAX_RATIO);
        } else {
            // 세로가 지나치게 긴 경우
            targetWidth = (int) (originalHeight * MIN_RATIO);
            targetHeight = originalHeight;
        }

        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();

        try {
            g2d.setColor(Color.BLACK); // 레터박스 배경을 검정으로 설정
            g2d.fillRect(0, 0, targetWidth, targetHeight);

            // 고품질 렌더링 옵션
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 원본 이미지를 중앙에 배치
            int x = (targetWidth - originalWidth) / 2;
            int y = (targetHeight - originalHeight) / 2;
            g2d.drawImage(original, x, y, null);
        } finally {
            g2d.dispose(); // 리소스 해제
        }

        return canvas;
    }

    private String uploadToR2(S3Client client, String bucket, String key, BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos); // 동적으로 이미지 포맷 처리
        byte[] bytes = baos.toByteArray();

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(SUPPORTED_FORMATS.get(format)) // 포맷에 맞는 MIME 타입 설정
                .contentLength((long) bytes.length)
                .build();

        client.putObject(req, RequestBody.fromBytes(bytes));

        // Public URL 반환 (필요 시 추가 처리 가능)
        return openUrl + "/" + key;
    }

    private S3Client buildR2Client(String endpoint, String accessKey, String secretKey) {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    private String extractFormatFromImage(String imageUrl) throws IOException {
        // URL 연결 열기
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream()) {
            // HTTP 헤더에서 Content-Type 확인
            String contentType = url.openConnection().getContentType();
            if (contentType == null || contentType.isBlank()) {
                throw new IOException("Content-Type을 감지할 수 없습니다.");
            }

            // Content-Type을 기반으로 포맷 추출
            for (Map.Entry<String, String> entry : SUPPORTED_FORMATS.entrySet()) {
                if (contentType.equalsIgnoreCase(entry.getValue())) { // Content-Type과 매칭
                    return entry.getKey(); // 확장자 반환 (e.g., "png", "jpg")
                }
            }

            // 지원하지 않는 포맷인 경우 예외
            throw new IllegalArgumentException("지원하지 않는 이미지 포맷: " + contentType);
        }
    }
}
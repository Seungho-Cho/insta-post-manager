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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

            // 이미지 리사이즈 처리
            BufferedImage resized = (original.getWidth() > MAX_WIDTH || original.getHeight() > MAX_HEIGHT)
                    ? Thumbnails.of(original).size(MAX_WIDTH, MAX_HEIGHT).asBufferedImage()
                    : original;

            // 원본 이미지 업로드
            result = uploadToR2(r2Client, bucket, BASE_DIR + saveName + "." + format, resized, format);

            // 썸네일 이미지 업로드
            BufferedImage thumb = Thumbnails.of(resized).size(THUMB_WIDTH, THUMB_HEIGHT).asBufferedImage();
            uploadToR2(r2Client, bucket, THUMB_DIR + saveName + "." + format, thumb, format);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
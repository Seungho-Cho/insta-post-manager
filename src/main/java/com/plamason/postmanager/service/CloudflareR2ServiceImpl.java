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
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

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
public class CloudflareR2ServiceImpl implements ImageHostingService{

    private final AppSettingService appSettingService;

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final int THUMB_WIDTH = 300;
    private static final int THUMB_HEIGHT = 300;

    String endpoint;
    String accessKey;
    String secretKey;
    String bucket;

    @PostConstruct
    public void init() {
        Map<String, String> appSettings = appSettingService.getAllSettings();
        endpoint = appSettings.get(AppSettingName.R2_ENDPOINT_URL.toString());
        accessKey = appSettings.get(AppSettingName.R2_ACCESS_TOKEN.toString());
        secretKey = appSettings.get(AppSettingName.R2_API_KEY.toString());
        bucket = appSettings.get(AppSettingName.R2_BUCKET_NAME.toString());

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
    }

    @Override
    public String uploadImage(String imageUrl, String saveName) {
        String result = null;
        try (InputStream in = new URL(imageUrl).openStream();
             S3Client r2Client = buildR2Client(endpoint, accessKey, secretKey)) {

            BufferedImage original = ImageIO.read(in);
            if (original == null) throw new IOException("이미지 로드 실패");

            BufferedImage resized = (original.getWidth() > MAX_WIDTH || original.getHeight() > MAX_HEIGHT)
                    ? Thumbnails.of(original).size(MAX_WIDTH, MAX_HEIGHT).asBufferedImage()
                    : original;

            result = uploadToR2(r2Client, bucket, saveName + ".jpg", resized);
            BufferedImage thumb = Thumbnails.of(resized).size(THUMB_WIDTH, THUMB_HEIGHT).asBufferedImage();
            uploadToR2(r2Client, bucket, saveName + "_thumb.jpg", thumb);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private S3Client buildR2Client(String endpoint, String accessKey, String secretKey) {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    private String uploadToR2(S3Client client, String bucket, String key, BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] bytes = baos.toByteArray();

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/jpeg")
                .contentLength((long) bytes.length)
                .build();

        PutObjectResponse putObjectResponse = client.putObject(req, RequestBody.fromBytes(bytes));
        return putObjectResponse.toString();
    }
}

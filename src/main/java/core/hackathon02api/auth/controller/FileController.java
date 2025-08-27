package core.hackathon02api.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final S3Client s3Client;
    private final core.hackathon02api.auth.config.S3Properties props;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String ext = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String base = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8);
            String key = "uploads/" + Instant.now().toEpochMilli() + "_" + base;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            String url = props.getPublicBase() + "/" + key;
            return ResponseEntity.ok().body(new UploadResponse(url));

        } catch (Exception e) {
            // ✅ 에러 로그 강제로 찍기
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("업로드 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    record UploadResponse(String url) {}
}

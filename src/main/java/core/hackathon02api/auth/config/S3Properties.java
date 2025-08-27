package core.hackathon02api.auth.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "aws.s3")

public class S3Properties {
    private String bucket;
    private String region;
    private String publicBase;
    private String accessKey;
    private String secretKey;
}

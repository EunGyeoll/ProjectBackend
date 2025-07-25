package Project.ProjectBackend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class S3Properties {
    private String defaultProfileUrl;

    public String getDefaultProfileUrl() {
        return defaultProfileUrl;
    }

    public void setDefaultProfileUrl(String defaultProfileUrl) {
        this.defaultProfileUrl = defaultProfileUrl;
    }

    @PostConstruct
    public void printDefaultUrl() {
        System.out.println("[확인] 기본 프로필 URL: " + defaultProfileUrl);
    }
}

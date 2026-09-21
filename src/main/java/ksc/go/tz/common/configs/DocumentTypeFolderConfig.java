package ksc.go.tz.common.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class DocumentTypeFolderConfig {
    private Map<String, String> paths;
}

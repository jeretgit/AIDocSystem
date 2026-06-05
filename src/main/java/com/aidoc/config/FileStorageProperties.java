package com.aidoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.upload")
public class FileStorageProperties {

    /**
     * 相对于项目运行目录的上传根路径，默认 uploads/
     */
    private String dir = "uploads";
}

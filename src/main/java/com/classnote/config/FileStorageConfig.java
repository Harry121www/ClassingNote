package com.classnote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorageConfig {

    @Value("${app.attachment-root-path:~/classnote-files}")
    private String attachmentRootPath;

    private Path resolvedPath;

    @PostConstruct
    public void init() throws IOException {
        String expanded = attachmentRootPath.replaceFirst("^~", System.getProperty("user.home"));
        resolvedPath = Paths.get(expanded).toAbsolutePath();
        Files.createDirectories(resolvedPath);
    }

    public Path getAttachmentRoot() {
        return resolvedPath;
    }
}

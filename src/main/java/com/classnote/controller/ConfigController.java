package com.classnote.controller;

import com.classnote.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    private final DataSource dataSource;

    @Value("${app.version}")
    private String version;

    @Value("${app.data-directory:~/.classnote}")
    private String dataDirectory;

    @Value("${app.attachment-root-path:~/classnote-files}")
    private String attachmentRootPath;

    public ConfigController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("version", version);
        config.put("dataDirectory", dataDirectory);
        config.put("attachmentRootPath", attachmentRootPath);
        config.put("maxUploadSize", 52428800);
        config.put("dbConnected", checkDb());
        return ApiResponse.success(config);
    }

    @GetMapping("/db-status")
    public ApiResponse<Map<String, Object>> dbStatus() {
        Map<String, Object> status = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            status.put("connected", true);
            status.put("url", conn.getMetaData().getURL());
            status.put("version", conn.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
        }
        return ApiResponse.success(status);
    }

    private boolean checkDb() {
        try (Connection conn = dataSource.getConnection()) {
            return conn != null;
        } catch (Exception e) {
            return false;
        }
    }
}

package com.classnote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class ClassNoteApplication {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(ClassNoteApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:8080";
        try {
            // Try Chrome app mode first (cleanest)
            try {
                new ProcessBuilder("cmd", "/c", "start", "chrome", "--app=" + url, "--start-maximized").start();
                return;
            } catch (Exception e1) { /* chrome not available */ }

            // Try Edge app mode
            try {
                new ProcessBuilder("cmd", "/c", "start", "msedge", "--app=" + url, "--start-maximized").start();
                return;
            } catch (Exception e2) { /* edge not available */ }

            // Fallback: system default browser
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            // Browser launch failed, user can open manually
        }
    }
}

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
        System.out.println("\n========================================");
        System.out.println("  ClassNote 已启动！");
        System.out.println("  请在浏览器打开: " + url);
        System.out.println("========================================\n");

        new Thread(() -> {
            try { Thread.sleep(2000); } catch (Exception e) { }
            try {
                // Try Desktop.browse first (most reliable in jpackage)
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                    return;
                }
            } catch (Exception e1) { /* fall through */ }

            try {
                // Try direct Chrome
                Runtime.getRuntime().exec(new String[]{
                    System.getenv("ProgramFiles") + "\\Google\\Chrome\\Application\\chrome.exe",
                    "--app=" + url, "--start-maximized"
                });
                return;
            } catch (Exception e2) { /* fall through */ }

            try {
                // Try direct Edge
                Runtime.getRuntime().exec(new String[]{
                    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                    "--app=" + url, "--start-maximized"
                });
            } catch (Exception e3) { /* give up */ }
        }).start();
    }
}

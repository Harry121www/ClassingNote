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
    public void openWindow() {
        System.out.println("Starting desktop window...");
        new Thread(() -> {
            try { Thread.sleep(1500); } catch (Exception e) { }
            DesktopWindow.launch("http://localhost:8080");
        }).start();
    }
}

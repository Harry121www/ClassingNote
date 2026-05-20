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
        DesktopWindow.show();
        SpringApplication.run(ClassNoteApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        System.out.println("Server ready, navigating to app...");
        DesktopWindow.navigateTo("http://localhost:8080");
    }
}

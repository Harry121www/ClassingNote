package com.classnote;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DesktopWindow extends Application {

    private static Stage primaryStage;
    private static WebView webView;
    private static volatile boolean ready = false;

    public static void show() {
        new Thread(() -> Application.launch(DesktopWindow.class)).start();
    }

    public static void navigateTo(String serverUrl) {
        ready = true;
        Platform.runLater(() -> webView.getEngine().load(serverUrl));
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        webView = new WebView();

        // Show loading screen immediately
        String loadingHtml = "<html><body style='background:#1a1a2e;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;'>"
            + "<div style='text-align:center;color:#e0e0e0;font-family:sans-serif;'>"
            + "<h2 style='color:#5b7cfa;'>ClassNote</h2>"
            + "<p>正在启动...</p></div></body></html>";
        webView.getEngine().loadContent(loadingHtml);

        double w = Screen.getPrimary().getVisualBounds().getWidth() * 0.85;
        double h = Screen.getPrimary().getVisualBounds().getHeight() * 0.88;

        Scene scene = new Scene(webView, w, h);
        stage.setScene(scene);
        stage.setTitle("ClassNote");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0); });
        stage.show();
    }
}

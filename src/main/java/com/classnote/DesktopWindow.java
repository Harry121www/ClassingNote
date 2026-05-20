package com.classnote;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Screen;

public class DesktopWindow extends Application {

    private static String url;

    public static void launch(String serverUrl) {
        url = serverUrl;
        new Thread(() -> Application.launch(DesktopWindow.class)).start();
    }

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        webView.getEngine().load(url);
        webView.getEngine().setOnError(e -> System.out.println("WebView error: " + e.getMessage()));

        double w = Screen.getPrimary().getVisualBounds().getWidth() * 0.85;
        double h = Screen.getPrimary().getVisualBounds().getHeight() * 0.88;

        Scene scene = new Scene(webView, w, h);
        stage.setScene(scene);
        stage.setTitle("ClassNote");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }
}

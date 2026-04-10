package com.studentexpensetracker.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public final class MainApp extends Application {
    @Override
    public void start(Stage aPrimaryStage) throws Exception {
        FXMLLoader aLoader = new FXMLLoader(
                Objects.requireNonNull(MainApp.class.getResource("/ui/MainWindow.fxml"))
        );
        Scene aScene = new Scene(aLoader.load(), 1280, 800);
        aScene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource("/ui/styles.css")).toExternalForm()
        );
        aPrimaryStage.setTitle("Student Budget & Expense Tracking System");
        aPrimaryStage.setScene(aScene);
        aPrimaryStage.show();
    }

    public static void main(String[] anArgs) {
        launch(anArgs);
    }
}

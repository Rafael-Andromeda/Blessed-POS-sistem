package app;

import database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        DatabaseInitializer.initialize();
        showLogin();
    }

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/view/login.fxml"));

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            Scene scene = new Scene(
                    loader.load(),
                    screenBounds.getWidth(),
                    screenBounds.getHeight()
            );

            scene.getStylesheets().add(
                    MainApp.class.getResource("/css/style.css")
                            .toExternalForm());

            primaryStage.setTitle("NasiGoreng 71 - Login");
            primaryStage.setScene(scene);

            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());

            // Langsung fullscreen window (tanpa fullscreen F11)
            primaryStage.setMaximized(true);

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showMain() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/view/main_layout.fxml"));

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            Scene scene = new Scene(
                    loader.load(),
                    screenBounds.getWidth(),
                    screenBounds.getHeight()
            );

            scene.getStylesheets().add(
                    MainApp.class.getResource("/css/style.css")
                            .toExternalForm());

            primaryStage.setTitle("NasiGoreng 71 - Point of Sale");
            primaryStage.setScene(scene);

            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());

            primaryStage.setMaximized(true);

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
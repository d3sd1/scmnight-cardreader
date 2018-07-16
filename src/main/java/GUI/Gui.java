package GUI;

import scm.App;

import java.io.IOException;
import java.util.Arrays;

import javafx.application.Application;

import static javafx.application.Application.launch;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import scm.Constants;

public class Gui extends Application {

    @Override
    public void start(Stage primaryStage) {

        try {
            Parent screen0 = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));
            primaryStage.initStyle(StageStyle.UNDECORATED);
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            int appWidth = (int) primaryScreenBounds.getWidth() / 2;
            int appHeight = (int) primaryScreenBounds.getHeight() / 2;

            primaryStage.getIcons().addAll(
                    new Image(Gui.class.getResourceAsStream("/img/logos/taskbar_16x16.png")),
                    new Image(Gui.class.getResourceAsStream("/img/logos/taskbar_32x32.png")),
                    new Image(Gui.class.getResourceAsStream("/img/logos/taskbar_48x48.png")),
                    new Image(Gui.class.getResourceAsStream("/img/logos/taskbar_64x64.png")),
                    new Image(Gui.class.getResourceAsStream("/img/logos/taskbar_128x128.png")),
                    new Image(Gui.class.getResourceAsStream("/img/logos/taskbar_256x256.png"))
            );

            Scene scene = new Scene(screen0, appWidth, appHeight);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setMinHeight(primaryStage.getHeight());
            primaryStage.setMinWidth(primaryStage.getWidth());

            Platform.setImplicitExit(false);
            primaryStage.setOnCloseRequest((WindowEvent event) -> {
                event.consume();
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        Constants.setEnv(args);
        launch();
    }

}

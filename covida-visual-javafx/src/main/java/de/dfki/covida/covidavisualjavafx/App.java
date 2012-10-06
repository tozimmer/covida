package de.dfki.covida.covidavisualjavafx;

import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.videovlcj.rendered.RenderedVideoHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * Hello world!
 *
 */
public class App extends Application implements IApplication{

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        RenderedVideoHandler video = new RenderedVideoHandler("../covida-res/videos/Collaborative Video Annotation.mp4", "Covida Demo", 500, 400);
        video.start();
        ImageView imageView = new ImageView();
        try {
            ByteArrayOutputStream tmp_out = new ByteArrayOutputStream();
            ImageIO.write(video.getVideoImage(), "PNG", tmp_out);
            InputStream tmp_in = new ByteArrayInputStream(tmp_out.toByteArray());
            Image image = new Image(tmp_in);
            imageView.setImage(image);
        } catch (Exception ex) {
        }


        

        root.getChildren().add(imageView);
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        primaryStage.setTitle("Covida");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public String getWindowTitle() {
        return "Covida";
    }

    public void start() {
        launch();
    }
}

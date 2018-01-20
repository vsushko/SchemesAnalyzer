package ru.vsushko.analyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created by vsa
 * Date: 11.11.14.
 */
public class Main extends Application {

    public static void main(String[] args) throws SAXException, IOException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("context.fxml"));
        stage.setTitle("xsd-analyzer v.1.0.1");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }
}

package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FxDemoApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Label label = new Label("用户名:");
        BorderPane borderPane = new BorderPane(label);

        Scene scene = new Scene(borderPane, 400, 400);

        stage.setTitle("FX Demo");
        stage.setScene(scene);
        stage.show();

    }

}

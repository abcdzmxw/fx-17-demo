package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FxDemoApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("FX Demo");
        stage.setScene(new Scene(new BorderPane(new Button("Hello, JavaFX!")),
                400, 400)
        );
        stage.show();

    }

}

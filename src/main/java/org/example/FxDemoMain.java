package org.example;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FxDemoMain {
    public static void main(String[] args) {

        SpringApplication.run(FxDemoMain.class, args);
        Application.launch(FxDemoApp.class, args);
    }
}
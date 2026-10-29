package com.example.backpropagationfx;

import javafx.application.Application;

/**
 * Inicializador da aplicação para contornar restrições de inicialização do JavaFX sem módulos no classpath.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}

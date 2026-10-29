package com.example.backpropagationfx;

import com.example.backpropagationfx.visao.TelaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Ponto de entrada da aplicação JavaFX.
 * Configura o palco principal, o tamanho da janela e aplica a folha de estilos CSS.
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage palcoPrincipal) {
        palcoPrincipal.setTitle("BackPropagationFX");

        TelaPrincipal telaPrincipal = new TelaPrincipal(palcoPrincipal);
        Scene cena = new Scene(telaPrincipal, 1200, 780);

        // Aplica o arquivo CSS de estilos modernos
        URL recursoCss = getClass().getResource("estilos.css");
        if (recursoCss != null) {
            cena.getStylesheets().add(recursoCss.toExternalForm());
        }

        palcoPrincipal.setScene(cena);
        palcoPrincipal.setMinWidth(1000);
        palcoPrincipal.setMinHeight(650);
        palcoPrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


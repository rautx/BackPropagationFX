package com.example.backpropagationfx.visao;

import com.example.backpropagationfx.modelo.RedeNeuralMLP;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;

/**
 * Componente visual em JavaFX que desenha graficamente a arquitetura da rede neural.
 * Exibe os neurônios em 3 colunas (Entrada, Oculta e Saída) e as conexões sinápticas.
 * A espessura da linha reflete a magnitude do peso e a cor reflete o sinal (positivo ou negativo).
 * Ao passar o mouse sobre uma conexão, exibe o valor do peso.
 */
public class PainelVisualizacaoRede extends Pane {

    private Label rotuloStatusPeso;

    public PainelVisualizacaoRede(Label rotuloStatus) {
        this.rotuloStatusPeso = rotuloStatus;
        this.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");
    }

    /**
     * Redesenha toda a estrutura da rede com as conexões e os valores dos pesos atuais.
     */
    public void redesenharRede(RedeNeuralMLP rede, ArrayList<String> nomesEntradas, ArrayList<String> nomesSaidas) {
        this.getChildren().clear();

        if (rede == null) {
            Text mensagemVazia = new Text(40, 60, "Rede ainda não inicializada.");
            mensagemVazia.setFill(Color.GRAY);
            this.getChildren().add(mensagemVazia);
        } else {
            double larguraTotal = this.getWidth();
            if (larguraTotal < 350.0) {
                larguraTotal = 450.0;
            }
            double alturaTotal = this.getHeight();
            if (alturaTotal < 250.0) {
                alturaTotal = 320.0;
            }

            int qtdEntradas = rede.getQuantidadeEntradas();
            int qtdOcultas = rede.getQuantidadeOcultas();
            int qtdSaidas = rede.getQuantidadeSaidas();

            double[][] pesosEO = rede.getPesosEntradaOculta();
            double[][] pesosOS = rede.getPesosOcultaSaida();

            // Posições X das três camadas
            double xEntrada = 60.0;
            double xOculta = larguraTotal / 2.0;
            double xSaida = larguraTotal - 60.0;

            // Arrays para guardar as coordenadas Y dos centros dos neurônios
            double[] yEntradas = new double[qtdEntradas];
            double[] yOcultas = new double[qtdOcultas];
            double[] ySaidas = new double[qtdSaidas];

            double raioNeuronio = 16.0;

            // Calcula Y para entradas
            double passoEntrada = alturaTotal / (double) (qtdEntradas + 1);
            for (int i = 0; i < qtdEntradas; i = i + 1) {
                yEntradas[i] = passoEntrada * (double) (i + 1);
            }

            // Calcula Y para ocultas
            double passoOculta = alturaTotal / (double) (qtdOcultas + 1);
            for (int i = 0; i < qtdOcultas; i = i + 1) {
                yOcultas[i] = passoOculta * (double) (i + 1);
            }

            // Calcula Y para saídas
            double passoSaida = alturaTotal / (double) (qtdSaidas + 1);
            for (int i = 0; i < qtdSaidas; i = i + 1) {
                ySaidas[i] = passoSaida * (double) (i + 1);
            }

            // 1. Desenha as conexões entre Entrada e Oculta
            for (int o = 0; o < qtdOcultas; o = o + 1) {
                for (int e = 0; e < qtdEntradas; e = e + 1) {
                    double peso = pesosEO[o][e];
                    Line conexao = new Line(xEntrada, yEntradas[e], xOculta, yOcultas[o]);

                    // Cor pelo sinal do peso
                    if (peso >= 0.0) {
                        conexao.setStroke(Color.rgb(0, 103, 41, 0.75)); // Verde Unoeste para positivo
                    } else {
                        conexao.setStroke(Color.rgb(220, 38, 38, 0.75)); // Vermelho para negativo
                    }

                    // Espessura pela magnitude absoluta do peso
                    double magnitude = Math.abs(peso);
                    double espessura = 1.0 + (magnitude * 2.0);
                    if (espessura > 5.0) {
                        espessura = 5.0;
                    }
                    conexao.setStrokeWidth(espessura);

                    String nomeOrigem = "X" + (e + 1);
                    if (nomesEntradas != null && e < nomesEntradas.size()) {
                        nomeOrigem = nomesEntradas.get(e);
                    }
                    String textoInfo = "w(" + nomeOrigem + " -> H" + (o + 1) + ") = " + String.format("%.6f", peso);

                    Tooltip dica = new Tooltip(textoInfo);
                    Tooltip.install(conexao, dica);

                    final double espessuraOriginal = espessura;
                    conexao.setOnMouseEntered(evento -> {
                        conexao.setStrokeWidth(espessuraOriginal + 3.0);
                        if (this.rotuloStatusPeso != null) {
                            this.rotuloStatusPeso.setText(textoInfo);
                        }
                    });

                    conexao.setOnMouseExited(evento -> {
                        conexao.setStrokeWidth(espessuraOriginal);
                    });

                    this.getChildren().add(conexao);
                }
            }

            // 2. Desenha as conexões entre Oculta e Saída
            for (int s = 0; s < qtdSaidas; s = s + 1) {
                for (int o = 0; o < qtdOcultas; o = o + 1) {
                    double peso = pesosOS[s][o];
                    Line conexao = new Line(xOculta, yOcultas[o], xSaida, ySaidas[s]);

                    if (peso >= 0.0) {
                        conexao.setStroke(Color.rgb(0, 103, 41, 0.75)); // Verde Unoeste para positivo
                    } else {
                        conexao.setStroke(Color.rgb(220, 38, 38, 0.75)); // Vermelho para negativo
                    }

                    double magnitude = Math.abs(peso);
                    double espessura = 1.0 + (magnitude * 2.0);
                    if (espessura > 5.0) {
                        espessura = 5.0;
                    }
                    conexao.setStrokeWidth(espessura);

                    String nomeDestino = "Y" + (s + 1);
                    if (nomesSaidas != null && s < nomesSaidas.size()) {
                        nomeDestino = nomesSaidas.get(s);
                    }
                    String textoInfo = "w(H" + (o + 1) + " -> " + nomeDestino + ") = " + String.format("%.6f", peso);

                    Tooltip dica = new Tooltip(textoInfo);
                    Tooltip.install(conexao, dica);

                    final double espessuraOriginal = espessura;
                    conexao.setOnMouseEntered(evento -> {
                        conexao.setStrokeWidth(espessuraOriginal + 3.0);
                        if (this.rotuloStatusPeso != null) {
                            this.rotuloStatusPeso.setText(textoInfo);
                        }
                    });

                    conexao.setOnMouseExited(evento -> {
                        conexao.setStrokeWidth(espessuraOriginal);
                    });

                    this.getChildren().add(conexao);
                }
            }

            // 3. Desenha os círculos dos neurônios por cima das conexões

            // Camada de Entrada
            for (int e = 0; e < qtdEntradas; e = e + 1) {
                Circle circulo = new Circle(xEntrada, yEntradas[e], raioNeuronio);
                circulo.setFill(Color.WHITE);
                circulo.setStroke(Color.rgb(16, 185, 129));
                circulo.setStrokeWidth(2.0);

                String nome = "X" + (e + 1);
                Text rotulo = new Text(nome);
                rotulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                rotulo.setFill(Color.rgb(30, 41, 59));
                rotulo.setX(xEntrada - (rotulo.getLayoutBounds().getWidth() / 2.0));
                rotulo.setY(yEntradas[e] + 4.0);

                this.getChildren().addAll(circulo, rotulo);
            }

            // Camada Oculta
            for (int o = 0; o < qtdOcultas; o = o + 1) {
                Circle circulo = new Circle(xOculta, yOcultas[o], raioNeuronio);
                circulo.setFill(Color.WHITE);
                circulo.setStroke(Color.rgb(234, 179, 8));
                circulo.setStrokeWidth(2.0);

                String nome = "H" + (o + 1);
                Text rotulo = new Text(nome);
                rotulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                rotulo.setFill(Color.rgb(30, 41, 59));
                rotulo.setX(xOculta - (rotulo.getLayoutBounds().getWidth() / 2.0));
                rotulo.setY(yOcultas[o] + 4.0);

                this.getChildren().addAll(circulo, rotulo);
            }

            // Camada de Saída
            for (int s = 0; s < qtdSaidas; s = s + 1) {
                Circle circulo = new Circle(xSaida, ySaidas[s], raioNeuronio);
                circulo.setFill(Color.WHITE);
                circulo.setStroke(Color.rgb(0, 103, 41));
                circulo.setStrokeWidth(2.0);

                String nome = "Y" + (s + 1);
                if (nomesSaidas != null && s < nomesSaidas.size()) {
                    nome = nomesSaidas.get(s);
                }
                Text rotulo = new Text(nome);
                rotulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                rotulo.setFill(Color.rgb(30, 41, 59));
                rotulo.setX(xSaida - (rotulo.getLayoutBounds().getWidth() / 2.0));
                rotulo.setY(ySaidas[s] + 4.0);

                this.getChildren().addAll(circulo, rotulo);
            }
        }
    }
}


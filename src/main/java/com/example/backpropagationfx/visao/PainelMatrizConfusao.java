package com.example.backpropagationfx.visao;

import com.example.backpropagationfx.modelo.MatrizConfusao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;

/**
 * Componente visual que desenha a Matriz de Confusão em uma grade interativa,
 * com células coloridas:
 * - Diagonal principal (acertos) destacada em tons de azul.
 * - Erros fora da diagonal destacados em tons suaves de vermelho.
 * - Totais calculados por linha e por coluna.
 */
public class PainelMatrizConfusao extends VBox {

    public PainelMatrizConfusao() {
        this.setSpacing(12);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER);
    }

    /**
     * Renderiza a matriz de confusão a partir dos dados calculados no teste.
     */
    public void desenharMatriz(MatrizConfusao matriz) {
        this.getChildren().clear();

        if (matriz == null) {
            Label labelVazio = new Label("Nenhum teste executado ainda.");
            labelVazio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            this.getChildren().add(labelVazio);
        } else {
            ArrayList<String> classes = matriz.getNomesClasses();
            int n = matriz.getQuantidadeClasses();

            GridPane grade = new GridPane();
            grade.setHgap(4);
            grade.setVgap(4);
            grade.setAlignment(Pos.CENTER);

            // Célula do canto superior esquerdo (Real \ Prevista)
            Label labelCanto = new Label("Real \\ Prev");
            labelCanto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            labelCanto.setStyle("-fx-text-fill: #475569; -fx-padding: 6;");
            grade.add(labelCanto, 0, 0);

            // Cabeçalho das colunas (Classes Previstas)
            for (int c = 0; c < n; c = c + 1) {
                Label rotuloColuna = new Label(classes.get(c));
                rotuloColuna.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                rotuloColuna.setAlignment(Pos.CENTER);
                rotuloColuna.setPrefWidth(55);
                rotuloColuna.setStyle("-fx-text-fill: #1e293b; -fx-background-color: #f1f5f9; -fx-padding: 6; -fx-background-radius: 4;");
                grade.add(rotuloColuna, c + 1, 0);
            }

            // Coluna de Total
            Label rotuloTotalCol = new Label("Total");
            rotuloTotalCol.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            rotuloTotalCol.setAlignment(Pos.CENTER);
            rotuloTotalCol.setPrefWidth(55);
            rotuloTotalCol.setStyle("-fx-text-fill: #0f172a; -fx-background-color: #e2e8f0; -fx-padding: 6; -fx-background-radius: 4;");
            grade.add(rotuloTotalCol, n + 1, 0);

            // Linhas com dados
            for (int l = 0; l < n; l = l + 1) {
                // Cabeçalho da linha (Classe Real)
                Label rotuloLinha = new Label(classes.get(l));
                rotuloLinha.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                rotuloLinha.setAlignment(Pos.CENTER_RIGHT);
                rotuloLinha.setPrefWidth(50);
                rotuloLinha.setStyle("-fx-text-fill: #1e293b; -fx-background-color: #f1f5f9; -fx-padding: 6; -fx-background-radius: 4;");
                grade.add(rotuloLinha, 0, l + 1);

                for (int c = 0; c < n; c = c + 1) {
                    int valor = matriz.getValor(l, c);
                    Label celula = new Label(String.valueOf(valor));
                    celula.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    celula.setAlignment(Pos.CENTER);
                    celula.setPrefWidth(55);
                    celula.setPrefHeight(38);

                    if (l == c) {
                        // Diagonal Principal: Acertos com destaque em Verde Unoeste
                        if (valor > 0) {
                            celula.setStyle("-fx-background-color: #006729; -fx-text-fill: #ffffff; -fx-background-radius: 6;");
                        } else {
                            celula.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #064e28; -fx-background-radius: 6;");
                        }
                    } else {
                        // Fora da diagonal: Erros
                        if (valor > 0) {
                            celula.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 6;");
                        } else {
                            celula.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #94a3b8; -fx-background-radius: 6; -fx-border-color: #f1f5f9; -fx-border-radius: 6;");
                        }
                    }

                    grade.add(celula, c + 1, l + 1);
                }

                // Total da linha
                int totalLinha = matriz.getTotalClasseReal(l);
                Label celulaTotalLinha = new Label(String.valueOf(totalLinha));
                celulaTotalLinha.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                celulaTotalLinha.setAlignment(Pos.CENTER);
                celulaTotalLinha.setPrefWidth(55);
                celulaTotalLinha.setPrefHeight(38);
                celulaTotalLinha.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-background-radius: 6;");
                grade.add(celulaTotalLinha, n + 1, l + 1);
            }

            // Linha inferior de Totais Previstos
            Label rotuloTotalLinha = new Label("Total");
            rotuloTotalLinha.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            rotuloTotalLinha.setAlignment(Pos.CENTER_RIGHT);
            rotuloTotalLinha.setPrefWidth(50);
            rotuloTotalLinha.setStyle("-fx-text-fill: #0f172a; -fx-background-color: #e2e8f0; -fx-padding: 6; -fx-background-radius: 4;");
            grade.add(rotuloTotalLinha, 0, n + 1);

            for (int c = 0; c < n; c = c + 1) {
                int totalColuna = matriz.getTotalClassePrevista(c);
                Label celulaTotalCol = new Label(String.valueOf(totalColuna));
                celulaTotalCol.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                celulaTotalCol.setAlignment(Pos.CENTER);
                celulaTotalCol.setPrefWidth(55);
                celulaTotalCol.setPrefHeight(38);
                celulaTotalCol.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-background-radius: 6;");
                grade.add(celulaTotalCol, c + 1, n + 1);
            }

            // Total Geral no canto inferior
            int totalGeral = matriz.getTotalGeral();
            Label celulaTotalGeral = new Label(String.valueOf(totalGeral));
            celulaTotalGeral.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            celulaTotalGeral.setAlignment(Pos.CENTER);
            celulaTotalGeral.setPrefWidth(55);
            celulaTotalGeral.setPrefHeight(38);
            celulaTotalGeral.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-background-radius: 6;");
            grade.add(celulaTotalGeral, n + 1, n + 1);

            // Legenda
            HBox legenda = new HBox(16);
            legenda.setAlignment(Pos.CENTER);

            HBox itemAcerto = new HBox(6);
            itemAcerto.setAlignment(Pos.CENTER_LEFT);
            Rectangle blocoVerde = new Rectangle(14, 14, Color.web("#006729"));
            blocoVerde.setArcWidth(4);
            blocoVerde.setArcHeight(4);
            Label textoAcerto = new Label("Acertos (diagonal principal)");
            textoAcerto.setFont(Font.font("Segoe UI", 11));
            itemAcerto.getChildren().addAll(blocoVerde, textoAcerto);

            HBox itemErro = new HBox(6);
            itemErro.setAlignment(Pos.CENTER_LEFT);
            Rectangle blocoVermelho = new Rectangle(14, 14, Color.web("#fee2e2"));
            blocoVermelho.setStroke(Color.web("#b91c1c"));
            blocoVermelho.setArcWidth(4);
            blocoVermelho.setArcHeight(4);
            Label textoErro = new Label("Erros de classificação");
            textoErro.setFont(Font.font("Segoe UI", 11));
            itemErro.getChildren().addAll(blocoVermelho, textoErro);

            legenda.getChildren().addAll(itemAcerto, itemErro);

            this.getChildren().addAll(grade, legenda);
        }
    }
}


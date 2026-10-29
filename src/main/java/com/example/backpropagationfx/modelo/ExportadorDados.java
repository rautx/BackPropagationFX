package com.example.backpropagationfx.modelo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Utilitário para exportar o histórico de erros do treinamento e a matriz de confusão
 * para arquivos CSV (requisito bônus).
 */
public class ExportadorDados {

    /**
     * Exporta as medições de cada época (época, erro médio, taxa eta e tempo) para CSV.
     */
    public static boolean exportarHistoricoErroCsv(File destino, ArrayList<RegistroEpoca> historico) {
        boolean sucesso = true;
        BufferedWriter escritor = null;
        try {
            escritor = new BufferedWriter(new FileWriter(destino));
            escritor.write("Epoca,ErroMedio,TaxaAprendizagem,TempoMs\n");

            int total = historico.size();
            for (int i = 0; i < total; i = i + 1) {
                RegistroEpoca reg = historico.get(i);
                String linha = reg.getNumeroEpoca() + "," +
                        String.format(java.util.Locale.US, "%.8f", reg.getErroMedio()) + "," +
                        String.format(java.util.Locale.US, "%.8f", reg.getTaxaAprendizagem()) + "," +
                        reg.getTempoMilissegundos() + "\n";
                escritor.write(linha);
            }

            escritor.close();
        } catch (Exception e) {
            sucesso = false;
            if (escritor != null) {
                try {
                    escritor.close();
                } catch (IOException ex) {
                    // Ignora
                }
            }
        }
        return sucesso;
    }

    /**
     * Exporta a matriz de confusão e suas métricas para arquivo CSV.
     */
    public static boolean exportarMatrizConfusaoCsv(File destino, MatrizConfusao matriz) {
        boolean sucesso = true;
        BufferedWriter escritor = null;
        try {
            escritor = new BufferedWriter(new FileWriter(destino));

            ArrayList<String> classes = matriz.getNomesClasses();
            int n = matriz.getQuantidadeClasses();

            // Cabeçalho
            escritor.write("ClasseReal");
            for (int c = 0; c < n; c = c + 1) {
                escritor.write("," + classes.get(c));
            }
            escritor.write(",Total\n");

            // Linhas de dados
            for (int l = 0; l < n; l = l + 1) {
                escritor.write(classes.get(l));
                for (int c = 0; c < n; c = c + 1) {
                    escritor.write("," + matriz.getValor(l, c));
                }
                escritor.write("," + matriz.getTotalClasseReal(l) + "\n");
            }

            // Linha de total previsto
            escritor.write("TotalPrevisto");
            for (int c = 0; c < n; c = c + 1) {
                escritor.write("," + matriz.getTotalClassePrevista(c));
            }
            escritor.write("," + matriz.getTotalGeral() + "\n\n");

            // Métricas
            escritor.write("AcuraciaGeral," + String.format(java.util.Locale.US, "%.2f%%", matriz.getAcuraciaGeral()) + "\n\n");

            escritor.write("Classe,Precisao,Revocacao\n");
            for (int i = 0; i < n; i = i + 1) {
                String linhaMetrica = classes.get(i) + "," +
                        String.format(java.util.Locale.US, "%.2f%%", matriz.getPrecisaoClasse(i)) + "," +
                        String.format(java.util.Locale.US, "%.2f%%", matriz.getRevocacaoClasse(i)) + "\n";
                escritor.write(linhaMetrica);
            }

            escritor.close();
        } catch (Exception e) {
            sucesso = false;
            if (escritor != null) {
                try {
                    escritor.close();
                } catch (IOException ex) {
                    // Ignora
                }
            }
        }
        return sucesso;
    }
}


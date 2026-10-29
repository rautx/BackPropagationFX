package com.example.backpropagationfx.modelo;

import java.util.ArrayList;

/**
 * Representa a matriz de confusão para avaliar o classificador.
 * As linhas representam as classes reais e as colunas as classes previstas.
 * Calcula métricas fundamentais: Acurácia Geral, Precisão e Revocação por classe.
 */
public class MatrizConfusao {

    // Nomes das classes
    private ArrayList<String> nomesClasses;

    // Quantidade de classes
    private int quantidadeClasses;

    // Matriz de contagem: [classeReal][classePrevista]
    private int[][] matriz;

    public MatrizConfusao(ArrayList<String> nomesClasses) {
        this.nomesClasses = nomesClasses;
        this.quantidadeClasses = nomesClasses.size();
        this.matriz = new int[this.quantidadeClasses][this.quantidadeClasses];

        // Zera a matriz
        for (int l = 0; l < this.quantidadeClasses; l = l + 1) {
            for (int c = 0; c < this.quantidadeClasses; c = c + 1) {
                this.matriz[l][c] = 0;
            }
        }
    }

    public ArrayList<String> getNomesClasses() {
        return this.nomesClasses;
    }

    public int getQuantidadeClasses() {
        return this.quantidadeClasses;
    }

    public int getValor(int linhaReal, int colunaPrevista) {
        return this.matriz[linhaReal][colunaPrevista];
    }

    /**
     * Registra uma previsão feita pela rede.
     */
    public void registrarPrevisao(int indiceReal, int indicePrevisto) {
        if (indiceReal >= 0 && indiceReal < this.quantidadeClasses) {
            if (indicePrevisto >= 0 && indicePrevisto < this.quantidadeClasses) {
                this.matriz[indiceReal][indicePrevisto] = this.matriz[indiceReal][indicePrevisto] + 1;
            }
        }
    }

    /**
     * Retorna a quantidade total de exemplos que realmente pertencem à classe indicada (soma da linha).
     */
    public int getTotalClasseReal(int indiceLinha) {
        int total = 0;
        for (int c = 0; c < this.quantidadeClasses; c = c + 1) {
            total = total + this.matriz[indiceLinha][c];
        }
        return total;
    }

    /**
     * Retorna a quantidade total de exemplos que a rede previu como sendo da classe indicada (soma da coluna).
     */
    public int getTotalClassePrevista(int indiceColuna) {
        int total = 0;
        for (int l = 0; l < this.quantidadeClasses; l = l + 1) {
            total = total + this.matriz[l][indiceColuna];
        }
        return total;
    }

    /**
     * Retorna o total geral de exemplos avaliados.
     */
    public int getTotalGeral() {
        int total = 0;
        for (int l = 0; l < this.quantidadeClasses; l = l + 1) {
            for (int c = 0; c < this.quantidadeClasses; c = c + 1) {
                total = total + this.matriz[l][c];
            }
        }
        return total;
    }

    /**
     * Retorna a soma de acertos (diagonal principal).
     */
    public int getTotalAcertos() {
        int totalAcertos = 0;
        for (int i = 0; i < this.quantidadeClasses; i = i + 1) {
            totalAcertos = totalAcertos + this.matriz[i][i];
        }
        return totalAcertos;
    }

    /**
     * Retorna a acurácia geral em porcentagem: (TotalAcertos / TotalGeral) * 100.
     */
    public double getAcuraciaGeral() {
        double acuracia = 0.0;
        int totalGeral = this.getTotalGeral();
        if (totalGeral > 0) {
            int acertos = this.getTotalAcertos();
            acuracia = ((double) acertos * 100.0) / ((double) totalGeral);
        }
        return acuracia;
    }

    /**
     * Retorna a Precisão da classe em porcentagem: AcertosDaClasse / TotalPrevistoParaAClasse * 100.
     */
    public double getPrecisaoClasse(int indiceClasse) {
        double precisao = 0.0;
        int totalPrevisto = this.getTotalClassePrevista(indiceClasse);
        if (totalPrevisto > 0) {
            int acertos = this.matriz[indiceClasse][indiceClasse];
            precisao = ((double) acertos * 100.0) / ((double) totalPrevisto);
        }
        return precisao;
    }

    /**
     * Retorna a Revocação (Recall) da classe em porcentagem: AcertosDaClasse / TotalRealDaClasse * 100.
     */
    public double getRevocacaoClasse(int indiceClasse) {
        double revocacao = 0.0;
        int totalReal = this.getTotalClasseReal(indiceClasse);
        if (totalReal > 0) {
            int acertos = this.matriz[indiceClasse][indiceClasse];
            revocacao = ((double) acertos * 100.0) / ((double) totalReal);
        }
        return revocacao;
    }
}


package com.example.backpropagationfx.modelo;

// representa uma única linha da base de dados
// contém os valores numéricos originais, os valores normalizados entre 0 e 1, o rótulo textual da classe e etc.
public class ExemploDado {

    // valores originais lidos do arquivo CSV
    private double[] atributosOriginais;

    // valores normalizados entre 0 e 1 pela fórmula Min-Max
    private double[] atributosNormalizados;

    // nome textual da classe (tipo "CA", "CB", etc)
    private String rotuloClasse;

    // vetor codificado (One-Hot) esperado para a saída da rede
    private double[] vetorDesejado;

    public ExemploDado(double[] atributosOriginais, String rotuloClasse) {
        this.atributosOriginais = atributosOriginais;
        this.rotuloClasse = rotuloClasse;
        int tamanho = atributosOriginais.length;
        this.atributosNormalizados = new double[tamanho];
    }

    public double[] getAtributosOriginais() {
        return this.atributosOriginais;
    }

    public void setAtributosOriginais(double[] atributosOriginais) {
        this.atributosOriginais = atributosOriginais;
    }

    public double[] getAtributosNormalizados() {
        return this.atributosNormalizados;
    }

    public void setAtributosNormalizados(double[] atributosNormalizados) {
        this.atributosNormalizados = atributosNormalizados;
    }

    public String getRotuloClasse() {
        return this.rotuloClasse;
    }

    public void setRotuloClasse(String rotuloClasse) {
        this.rotuloClasse = rotuloClasse;
    }

    public double[] getVetorDesejado() {
        return this.vetorDesejado;
    }

    public void setVetorDesejado(double[] vetorDesejado) {
        this.vetorDesejado = vetorDesejado;
    }
}


package com.example.backpropagationfx.modelo;

/**
 * Guarda os dados de medição de uma época de treinamento.
 * Utilizado para atualizar gráficos em tempo real, tabelas e exportar para CSV.
 */
public class RegistroEpoca {

    private int numeroEpoca;
    private double erroMedio;
    private double taxaAprendizagem;
    private long tempoMilissegundos;

    public RegistroEpoca(int numeroEpoca, double erroMedio, double taxaAprendizagem, long tempoMilissegundos) {
        this.numeroEpoca = numeroEpoca;
        this.erroMedio = erroMedio;
        this.taxaAprendizagem = taxaAprendizagem;
        this.tempoMilissegundos = tempoMilissegundos;
    }

    public int getNumeroEpoca() {
        return this.numeroEpoca;
    }

    public double getErroMedio() {
        return this.erroMedio;
    }

    public double getTaxaAprendizagem() {
        return this.taxaAprendizagem;
    }

    public long getTempoMilissegundos() {
        return this.tempoMilissegundos;
    }
}


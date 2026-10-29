package com.example.backpropagationfx.modelo;

import java.util.ArrayList;

/**
 * Interface de retorno (callback) para notificar a interface gráfica sobre o progresso do treinamento,
 * ocorrência de platô e término.
 */
public interface OuvinteTreinamento {
    void aoAtualizarProgresso(int epoca, double erroMedio, double taxaAtual, long tempoDecorrido);
    void aoDetectarPlato(double desvioPadrao, double taxaAtual);
    void aoFinalizarTreinamento(String motivoFinalizacao, int epocasExecutadas, double erroFinal);
}


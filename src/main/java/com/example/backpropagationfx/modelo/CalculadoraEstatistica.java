package com.example.backpropagationfx.modelo;

import java.util.ArrayList;

// cálculos de média e desvio padrão, usados na detecção de platô
public class CalculadoraEstatistica {

    public static double calcularMedia(ArrayList<Double> valores) {
        double media = 0.0;
        int tamanho = valores.size();
        if (tamanho > 0) {
            double soma = 0.0;
            for (int i = 0; i < tamanho; i = i + 1) {
                soma = soma + valores.get(i);
            }
            media = soma / (double) tamanho;
        }
        return media;
    }

    public static double calcularDesvioPadrao(ArrayList<Double> valores) {
        double desvioPadrao = 0.0;
        int tamanho = valores.size();
        if (tamanho > 1) {
            double media = calcularMedia(valores);
            double somaDiferencasQuadradas = 0.0;

            for (int i = 0; i < tamanho; i = i + 1) {
                double diferenca = valores.get(i) - media;
                somaDiferencasQuadradas = somaDiferencasQuadradas + (diferenca * diferenca);
            }

            // variância (usando N - 1 para desvio padrão amostral)
            double variancia = somaDiferencasQuadradas / (double) (tamanho - 1);
            desvioPadrao = Math.sqrt(variancia);
        }
        return desvioPadrao;
    }
}


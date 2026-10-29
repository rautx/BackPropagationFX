package com.example.backpropagationfx;

import com.example.backpropagationfx.modelo.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TesteIntegracao {

    @Test
    public void testarFluxoCompleto() {
        File arquivoTreino = new File("testes/base_treinamento.csv");
        File arquivoTeste = new File("testes/base_teste.csv");

        assertTrue(arquivoTreino.exists(), "Arquivo de treino deve existir");
        assertTrue(arquivoTeste.exists(), "Arquivo de teste deve existir");

        // 1. Carregamento
        ConjuntoDados treino = new ConjuntoDados();
        ConjuntoDados teste = new ConjuntoDados();

        boolean leuTreino = treino.carregarArquivoCsv(arquivoTreino);
        boolean leuTeste = teste.carregarArquivoCsv(arquivoTeste);

        assertTrue(leuTreino, "Deve carregar o treino");
        assertTrue(leuTeste, "Deve carregar o teste");

        assertEquals(6, treino.getQuantidadeAtributos(), "Deve ter 6 atributos");
        assertEquals(5, treino.getQuantidadeClasses(), "Deve ter 5 classes");
        assertEquals(827, treino.getQuantidadeExemplos(), "Deve ter 827 exemplos de treino");

        // 2. Normalização Min-Max
        treino.calcularMinimosEMaximos();
        double[] min = treino.getMenoresValores();
        double[] max = treino.getMaioresValores();

        treino.normalizar(min, max);
        teste.normalizar(min, max);

        // 3. One-Hot
        int tipoFuncao = 2; // Logística
        treino.codificarSaidasOneHot(tipoFuncao, treino.getClassesUnicas());
        teste.codificarSaidasOneHot(tipoFuncao, treino.getClassesUnicas());

        // 4. Criação da Rede MLP (6 entradas, 6 ocultas, 5 saídas)
        RedeNeuralMLP rede = new RedeNeuralMLP(6, 6, 5, tipoFuncao);
        rede.inicializarPesosAleatorios(true, 42);

        // 5. Treinamento de 15 épocas
        double taxa = 0.2;
        double erroInicial = 0.0;
        double erroFinal = 0.0;

        ArrayList<ExemploDado> exemplos = treino.getExemplos();
        for (int epoca = 1; epoca <= 15; epoca = epoca + 1) {
            double somaErro = 0.0;
            for (int i = 0; i < exemplos.size(); i = i + 1) {
                ExemploDado ex = exemplos.get(i);
                rede.treinarExemplo(ex.getAtributosNormalizados(), ex.getVetorDesejado(), taxa);
                somaErro = somaErro + rede.calcularErroExemplo(ex.getVetorDesejado());
            }
            double erroMedio = somaErro / (double) exemplos.size();
            if (epoca == 1) {
                erroInicial = erroMedio;
            }
            erroFinal = erroMedio;
        }

        System.out.println("Erro Inicial: " + erroInicial + " | Erro após 15 épocas: " + erroFinal);
        assertTrue(erroFinal < erroInicial, "O erro médio deve diminuir com o treinamento");

        // 6. Teste e Matriz de Confusão
        MatrizConfusao matriz = new MatrizConfusao(treino.getClassesUnicas());
        ArrayList<ExemploDado> exemplosTeste = teste.getExemplos();
        for (int i = 0; i < exemplosTeste.size(); i = i + 1) {
            ExemploDado ex = exemplosTeste.get(i);
            rede.propagarEntradas(ex.getAtributosNormalizados());
            int previsto = rede.obterIndiceClassePrevista();

            String realRotulo = ex.getRotuloClasse();
            int realIndice = -1;
            boolean achou = false;
            int k = 0;
            while (k < treino.getClassesUnicas().size() && !achou) {
                if (treino.getClassesUnicas().get(k).equals(realRotulo)) {
                    realIndice = k;
                    achou = true;
                }
                k = k + 1;
            }
            matriz.registrarPrevisao(realIndice, previsto);
        }

        double acuracia = matriz.getAcuraciaGeral();
        System.out.println("Acurácia no Teste após 15 épocas: " + String.format("%.2f%%", acuracia));
        assertTrue(acuracia > 0.0, "Acurácia deve ser maior que 0%");
    }
}

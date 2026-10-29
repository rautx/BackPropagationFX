package com.example.backpropagationfx.modelo;

import java.util.Random;

/**
 * Implementação autoral da Rede Neural Artificial do tipo Perceptron Multicamadas (MLP).
 * Realiza propagação direta (forward), cálculo de gradientes de erro,
 * retropropagação (backward) e atualização de pesos sinápticos padrão a padrão.
 */
public class RedeNeuralMLP {

    // Quantidade de neurônios na camada de entrada (igual ao número de atributos)
    private int quantidadeEntradas;

    // Quantidade de neurônios na camada oculta
    private int quantidadeOcultas;

    // Quantidade de neurônios na camada de saída (igual ao número de classes)
    private int quantidadeSaidas;

    // Tipo de função de transferência escolhida: 1 = Linear, 2 = Logística, 3 = Tangente Hiperbólica
    private int tipoFuncaoAtivacao;

    // Neurônios da camada oculta
    private Neuronio[] neuroniosOcultos;

    // Neurônios da camada de saída
    private Neuronio[] neuroniosSaida;

    // Matriz de pesos ligando as entradas aos neurônios ocultos: [indiceOculto][indiceEntrada]
    private double[][] pesosEntradaOculta;

    // Matriz de pesos ligando os neurônios ocultos aos de saída: [indiceSaida][indiceOculto]
    private double[][] pesosOcultaSaida;

    public RedeNeuralMLP(int qtdEntradas, int qtdOcultas, int qtdSaidas, int tipoFuncao) {
        this.quantidadeEntradas = qtdEntradas;
        this.quantidadeOcultas = qtdOcultas;
        this.quantidadeSaidas = qtdSaidas;
        this.tipoFuncaoAtivacao = tipoFuncao;

        this.neuroniosOcultos = new Neuronio[qtdOcultas];
        for (int i = 0; i < qtdOcultas; i = i + 1) {
            this.neuroniosOcultos[i] = new Neuronio();
        }

        this.neuroniosSaida = new Neuronio[qtdSaidas];
        for (int i = 0; i < qtdSaidas; i = i + 1) {
            this.neuroniosSaida[i] = new Neuronio();
        }

        this.pesosEntradaOculta = new double[qtdOcultas][qtdEntradas];
        this.pesosOcultaSaida = new double[qtdSaidas][qtdOcultas];
    }

    public int getQuantidadeEntradas() {
        return this.quantidadeEntradas;
    }

    public int getQuantidadeOcultas() {
        return this.quantidadeOcultas;
    }

    public int getQuantidadeSaidas() {
        return this.quantidadeSaidas;
    }

    public int getTipoFuncaoAtivacao() {
        return this.tipoFuncaoAtivacao;
    }

    public void setTipoFuncaoAtivacao(int tipo) {
        this.tipoFuncaoAtivacao = tipo;
    }

    public double[][] getPesosEntradaOculta() {
        return this.pesosEntradaOculta;
    }

    public double[][] getPesosOcultaSaida() {
        return this.pesosOcultaSaida;
    }

    public Neuronio[] getNeuroniosOcultos() {
        return this.neuroniosOcultos;
    }

    public Neuronio[] getNeuroniosSaida() {
        return this.neuroniosSaida;
    }

    /**
     * Inicializa os pesos com valores aleatórios pequenos no intervalo [-0.5, +0.5].
     * Pode receber uma semente específica para permitir a repetição do experimento.
     */
    public void inicializarPesosAleatorios(boolean usarSeed, long semente) {
        Random gerador = null;
        if (usarSeed) {
            gerador = new Random(semente);
        } else {
            gerador = new Random();
        }

        // Pesos entre entrada e oculta
        for (int o = 0; o < this.quantidadeOcultas; o = o + 1) {
            for (int e = 0; e < this.quantidadeEntradas; e = e + 1) {
                // Gera valor no intervalo [-0.5, +0.5]
                double aleatorio = (gerador.nextDouble() * 1.0) - 0.5;
                this.pesosEntradaOculta[o][e] = aleatorio;
            }
        }

        // Pesos entre oculta e saída
        for (int s = 0; s < this.quantidadeSaidas; s = s + 1) {
            for (int o = 0; o < this.quantidadeOcultas; o = o + 1) {
                // Gera valor no intervalo [-0.5, +0.5]
                double aleatorio = (gerador.nextDouble() * 1.0) - 0.5;
                this.pesosOcultaSaida[s][o] = aleatorio;
            }
        }
    }

    /**
     * Etapa 1: Propagação direta (Forward).
     * Calcula o net e a saída ativada para cada neurônio oculto e depois para cada neurônio de saída.
     */
    public void propagarEntradas(double[] vetorEntradas) {
        // 1. Calcula saídas da camada oculta
        for (int o = 0; o < this.quantidadeOcultas; o = o + 1) {
            double somaNet = 0.0;
            for (int e = 0; e < this.quantidadeEntradas; e = e + 1) {
                somaNet = somaNet + (vetorEntradas[e] * this.pesosEntradaOculta[o][e]);
            }
            this.neuroniosOcultos[o].setNet(somaNet);
            this.neuroniosOcultos[o].aplicarAtivacao(this.tipoFuncaoAtivacao);
        }

        // 2. Calcula saídas da camada de saída a partir das saídas da oculta
        for (int s = 0; s < this.quantidadeSaidas; s = s + 1) {
            double somaNet = 0.0;
            for (int o = 0; o < this.quantidadeOcultas; o = o + 1) {
                double saidaOculta = this.neuroniosOcultos[o].getSaida();
                somaNet = somaNet + (saidaOculta * this.pesosOcultaSaida[s][o]);
            }
            this.neuroniosSaida[s].setNet(somaNet);
            this.neuroniosSaida[s].aplicarAtivacao(this.tipoFuncaoAtivacao);
        }
    }

    /**
     * Etapa 2: Calcula o erro (gradiente) nos neurônios da camada de saída.
     * Fórmula: Erro_G = (Desejado_G - Obtido_G) * f'(net_G)
     */
    public void calcularGradienteSaida(double[] vetorDesejado) {
        for (int s = 0; s < this.quantidadeSaidas; s = s + 1) {
            double desejado = vetorDesejado[s];
            double obtido = this.neuroniosSaida[s].getSaida();
            double derivada = this.neuroniosSaida[s].getDerivada();

            double erro = (desejado - obtido) * derivada;
            this.neuroniosSaida[s].setErroGradiente(erro);
        }
    }

    /**
     * Etapa 3: Retropropaga o erro para a camada oculta.
     * Fórmula: Erro_C = (Soma sobre G de Erro_G * w_G,C) * f'(net_C)
     */
    public void retropropagarErroParaOculta() {
        for (int o = 0; o < this.quantidadeOcultas; o = o + 1) {
            double somaPonderada = 0.0;
            for (int s = 0; s < this.quantidadeSaidas; s = s + 1) {
                double erroSaida = this.neuroniosSaida[s].getErroGradiente();
                double peso = this.pesosOcultaSaida[s][o];
                somaPonderada = somaPonderada + (erroSaida * peso);
            }

            double derivadaOculta = this.neuroniosOcultos[o].getDerivada();
            double erroOculta = somaPonderada * derivadaOculta;
            this.neuroniosOcultos[o].setErroGradiente(erroOculta);
        }
    }

    /**
     * Etapa 4: Atualiza todos os pesos da rede com base na taxa de aprendizagem (eta).
     * w_novo = w_atual + eta * Erro_destino * i_origem
     */
    public void atualizarPesos(double[] vetorEntradas, double taxaAprendizagem) {
        // Atualiza pesos entre a camada oculta e a de saída
        for (int s = 0; s < this.quantidadeSaidas; s = s + 1) {
            double erroDestino = this.neuroniosSaida[s].getErroGradiente();
            for (int o = 0; o < this.quantidadeOcultas; o = o + 1) {
                double saidaOrigem = this.neuroniosOcultos[o].getSaida();
                double delta = taxaAprendizagem * erroDestino * saidaOrigem;
                this.pesosOcultaSaida[s][o] = this.pesosOcultaSaida[s][o] + delta;
            }
        }

        // Atualiza pesos entre a camada de entrada e a oculta
        for (int o = 0; o < this.quantidadeOcultas; o = o + 1) {
            double erroDestino = this.neuroniosOcultos[o].getErroGradiente();
            for (int e = 0; e < this.quantidadeEntradas; e = e + 1) {
                double entradaOrigem = vetorEntradas[e];
                double delta = taxaAprendizagem * erroDestino * entradaOrigem;
                this.pesosEntradaOculta[o][e] = this.pesosEntradaOculta[o][e] + delta;
            }
        }
    }

    /**
     * Executa um passo completo de treinamento para um único exemplo (padrão a padrão).
     */
    public void treinarExemplo(double[] vetorEntradas, double[] vetorDesejado, double taxaAprendizagem) {
        this.propagarEntradas(vetorEntradas);
        this.calcularGradienteSaida(vetorDesejado);
        this.retropropagarErroParaOculta();
        this.atualizarPesos(vetorEntradas, taxaAprendizagem);
    }

    /**
     * Calcula o erro quadrático para o exemplo apresentado:
     * E = 0.5 * soma((Desejado - Obtido)^2) sobre todos os neurônios de saída.
     */
    public double calcularErroExemplo(double[] vetorDesejado) {
        double somaQuadrados = 0.0;
        for (int s = 0; s < this.quantidadeSaidas; s = s + 1) {
            double diferenca = vetorDesejado[s] - this.neuroniosSaida[s].getSaida();
            somaQuadrados = somaQuadrados + (diferenca * diferenca);
        }
        double erroExemplo = 0.5 * somaQuadrados;
        return erroExemplo;
    }

    /**
     * Retorna o índice da classe prevista pela rede (neurônio de saída com maior valor).
     */
    public int obterIndiceClassePrevista() {
        int indiceMaior = 0;
        double maiorValor = this.neuroniosSaida[0].getSaida();

        for (int s = 1; s < this.quantidadeSaidas; s = s + 1) {
            double valorAtual = this.neuroniosSaida[s].getSaida();
            if (valorAtual > maiorValor) {
                maiorValor = valorAtual;
                indiceMaior = s;
            }
        }

        return indiceMaior;
    }
}


package com.example.backpropagationfx.modelo;

/**
 * Representa um neurônio individual na rede neural artificial.
 * Guarda o valor de net (soma ponderada), a saída ativada,
 * a derivada e o erro/gradiente calculado no backpropagation.
 */
public class Neuronio {

    // Soma ponderada das entradas multiplicadas pelos pesos
    private double net;

    // Saída do neurônio após a aplicação da função de ativação
    private double saida;

    // Valor da derivada da função de ativação avaliada na saída
    private double derivada;

    // Gradiente do erro calculado para este neurônio
    private double erroGradiente;

    public Neuronio() {
        this.net = 0.0;
        this.saida = 0.0;
        this.derivada = 0.0;
        this.erroGradiente = 0.0;
    }

    public double getNet() {
        return this.net;
    }

    public void setNet(double net) {
        this.net = net;
    }

    public double getSaida() {
        return this.saida;
    }

    public void setSaida(double saida) {
        this.saida = saida;
    }

    public double getDerivada() {
        return this.derivada;
    }

    public void setDerivada(double derivada) {
        this.derivada = derivada;
    }

    public double getErroGradiente() {
        return this.erroGradiente;
    }

    public void setErroGradiente(double erroGradiente) {
        this.erroGradiente = erroGradiente;
    }

    /**
     * Aplica a função de transferência e calcula sua respectiva derivada.
     * Tipos permitidos:
     * 1 = Linear: f(net) = net / 10, f'(net) = 1 / 10
     * 2 = Logística: f(net) = 1 / (1 + e^-net), f'(net) = saida * (1 - saida)
     * 3 = Tangente Hiperbólica: f(net) = (1 - e^-2net) / (1 + e^-2net), f'(net) = 1 - saida^2
     */
    public void aplicarAtivacao(int tipoFuncao) {
        if (tipoFuncao == 1) {
            // Função Linear
            this.saida = this.net / 10.0;
            this.derivada = 0.1;
        } else {
            if (tipoFuncao == 2) {
                // Função Logística (Sigmoide)
                double expNegativo = Math.exp(-this.net);
                this.saida = 1.0 / (1.0 + expNegativo);
                this.derivada = this.saida * (1.0 - this.saida);
            } else {
                // Função Tangente Hiperbólica
                double expMenos2Net = Math.exp(-2.0 * this.net);
                this.saida = (1.0 - expMenos2Net) / (1.0 + expMenos2Net);
                this.derivada = 1.0 - (this.saida * this.saida);
            }
        }
    }
}


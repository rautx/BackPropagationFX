package com.example.backpropagationfx.modelo;

import java.util.ArrayList;

/**
 * Responsável por gerenciar a rotina de treinamento da MLP em uma thread separada.
 * Controla os laços das épocas, cálculo do erro médio da rede,
 * detecção de estagnação (platô), pausa, retomada e critérios de parada.
 */
public class TreinadorMLP implements Runnable {

    private RedeNeuralMLP rede;
    private ConjuntoDados dadosTreino;
    private OuvinteTreinamento ouvinte;

    // Configurações do treinamento
    private double taxaAprendizagem;
    private boolean usarCriterioEpocas;
    private int maximoEpocas;
    private boolean usarCriterioErro;
    private double erroMinimoLimiar;
    private double limiarDesvioPlato;

    // Flags de controle de fluxo de execução
    private volatile boolean executando;
    private volatile boolean pausado;
    private volatile boolean interrompido;
    private volatile boolean aguardandoDecisaoPlato;

    // Histórico de medições
    private ArrayList<RegistroEpoca> historicoTreinamento;
    private ArrayList<Double> janelaErrosPlato;

    // Métricas finais
    private int totalEpocasExecutadas;
    private double erroMedioFinal;
    private String motivoEncerramento;

    public TreinadorMLP(RedeNeuralMLP rede,
                        ConjuntoDados dadosTreino,
                        double taxaInicial,
                        boolean usarEpocas,
                        int maxEpocas,
                        boolean usarErro,
                        double erroMin,
                        OuvinteTreinamento ouvinte) {
        this.rede = rede;
        this.dadosTreino = dadosTreino;
        this.taxaAprendizagem = taxaInicial;
        this.usarCriterioEpocas = usarEpocas;
        this.maximoEpocas = maxEpocas;
        this.usarCriterioErro = usarErro;
        this.erroMinimoLimiar = erroMin;
        this.limiarDesvioPlato = 0.00001; // Limiar padrão exigido no enunciado
        this.ouvinte = ouvinte;

        this.executando = false;
        this.pausado = false;
        this.interrompido = false;
        this.aguardandoDecisaoPlato = false;

        this.historicoTreinamento = new ArrayList<RegistroEpoca>();
        this.janelaErrosPlato = new ArrayList<Double>();

        this.totalEpocasExecutadas = 0;
        this.erroMedioFinal = 0.0;
        this.motivoEncerramento = "";
    }

    public double getTaxaAprendizagem() {
        return this.taxaAprendizagem;
    }

    public void setTaxaAprendizagem(double taxa) {
        this.taxaAprendizagem = taxa;
    }

    public ArrayList<RegistroEpoca> getHistoricoTreinamento() {
        return this.historicoTreinamento;
    }

    public int getTotalEpocasExecutadas() {
        return this.totalEpocasExecutadas;
    }

    public double getErroMedioFinal() {
        return this.erroMedioFinal;
    }

    public String getMotivoEncerramento() {
        return this.motivoEncerramento;
    }

    public boolean isExecutando() {
        return this.executando;
    }

    public boolean isPausado() {
        return this.pausado;
    }

    public void pausar() {
        this.pausado = true;
    }

    public void retomar() {
        this.pausado = false;
        this.aguardandoDecisaoPlato = false;
    }

    public void interromper() {
        this.interrompido = true;
        this.pausado = false;
        this.aguardandoDecisaoPlato = false;
    }

    /**
     * Resposta do usuário quando um platô é detectado:
     * opcao 1: Interromper treinamento.
     * opcao 2: Continuar sem alterar eta.
     * opcao 3: Continuar reduzindo eta em 10% (eta = 0.9 * eta).
     */
    public void aplicarDecisaoPlato(int opcao) {
        if (opcao == 1) {
            // Interromper
            this.interromper();
        } else {
            if (opcao == 2) {
                // Continuar sem alterar
                this.janelaErrosPlato.clear(); // Limpa a janela para não disparar no mesmo instante
                this.retomar();
            } else {
                if (opcao == 3) {
                    // Reduz em 10%
                    this.taxaAprendizagem = this.taxaAprendizagem * 0.9;
                    this.janelaErrosPlato.clear();
                    this.retomar();
                }
            }
        }
    }

    @Override
    public void run() {
        this.executando = true;
        this.interrompido = false;
        this.pausado = false;
        this.aguardandoDecisaoPlato = false;

        long tempoInicio = System.currentTimeMillis();
        int epocaAtual = 1;
        boolean deveContinuar = true;

        ArrayList<ExemploDado> listaExemplos = this.dadosTreino.getExemplos();
        int totalExemplos = listaExemplos.size();

        while (deveContinuar) {

            // Tratamento de pausa do treinamento utilizando laço com flag
            while (this.pausado && !this.interrompido) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    this.interrompido = true;
                }
            }

            if (this.interrompido) {
                deveContinuar = false;
                this.motivoEncerramento = "Treinamento interrompido pelo usuário.";
            } else {
                double somaErrosExemplos = 0.0;

                // Percorre os exemplos de treino (padrão a padrão)
                for (int i = 0; i < totalExemplos; i = i + 1) {
                    ExemploDado exemplo = listaExemplos.get(i);
                    double[] entradas = exemplo.getAtributosNormalizados();
                    double[] desejados = exemplo.getVetorDesejado();

                    // Treina o exemplo atual
                    this.rede.treinarExemplo(entradas, desejados, this.taxaAprendizagem);

                    // Acumula o erro quadrático
                    double erroExemplo = this.rede.calcularErroExemplo(desejados);
                    somaErrosExemplos = somaErrosExemplos + erroExemplo;
                }

                // Erro médio da rede nesta época
                double erroMedioEpoca = somaErrosExemplos / (double) totalExemplos;
                this.erroMedioFinal = erroMedioEpoca;
                this.totalEpocasExecutadas = epocaAtual;

                long tempoDecorrido = System.currentTimeMillis() - tempoInicio;

                // Registra no histórico
                RegistroEpoca registro = new RegistroEpoca(epocaAtual, erroMedioEpoca, this.taxaAprendizagem, tempoDecorrido);
                this.historicoTreinamento.add(registro);

                // Notifica a interface gráfica
                if (this.ouvinte != null) {
                    this.ouvinte.aoAtualizarProgresso(epocaAtual, erroMedioEpoca, this.taxaAprendizagem, tempoDecorrido);
                }

                // Gerenciamento da janela de erros para detecção de platô (mínimo 10 épocas)
                this.janelaErrosPlato.add(erroMedioEpoca);
                if (this.janelaErrosPlato.size() > 10) {
                    this.janelaErrosPlato.remove(0);
                }

                if (this.janelaErrosPlato.size() == 10 && !this.aguardandoDecisaoPlato) {
                    double desvioPadrao = CalculadoraEstatistica.calcularDesvioPadrao(this.janelaErrosPlato);
                    if (desvioPadrao <= this.limiarDesvioPlato && desvioPadrao >= 0.0) {
                        this.pausado = true;
                        this.aguardandoDecisaoPlato = true;
                        if (this.ouvinte != null) {
                            this.ouvinte.aoDetectarPlato(desvioPadrao, this.taxaAprendizagem);
                        }
                    }
                }

                // Verificação dos critérios de parada
                if (this.usarCriterioErro) {
                    if (erroMedioEpoca <= this.erroMinimoLimiar) {
                        deveContinuar = false;
                        this.motivoEncerramento = "Critério de parada atingido: Limiar de erro mínimo alcançado (" + String.format("%.6f", erroMedioEpoca) + ").";
                    }
                }

                if (deveContinuar && this.usarCriterioEpocas) {
                    if (epocaAtual >= this.maximoEpocas) {
                        deveContinuar = false;
                        this.motivoEncerramento = "Critério de parada atingido: Número máximo de épocas executadas (" + this.maximoEpocas + ").";
                    }
                }

                epocaAtual = epocaAtual + 1;

                // Pequeno intervalo para permitir atualização fluida da tela e não sobrecarregar a CPU
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    this.interrompido = true;
                }
            }
        }

        this.executando = false;
        this.pausado = false;

        if (this.ouvinte != null) {
            this.ouvinte.aoFinalizarTreinamento(this.motivoEncerramento, this.totalEpocasExecutadas, this.erroMedioFinal);
        }
    }
}


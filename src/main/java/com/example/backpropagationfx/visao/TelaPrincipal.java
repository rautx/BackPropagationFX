package com.example.backpropagationfx.visao;

import com.example.backpropagationfx.modelo.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

/**
 * Interface gráfica principal construída em JavaFX.
 * Organizada em abas funcionais:
 * 1. Base de Dados: Carregamento, divisão e normalização dos dados.
 * 2. Configuração & Treinamento: Arquitetura da rede, funções, taxa eta, gráficos e platô.
 * 3. Avaliação & Resultados: Matriz de confusão, acurácia, precisão, revocação e exportação.
 */
public class TelaPrincipal extends BorderPane implements OuvinteTreinamento {

    private Stage palcoPrincipal;

    // Conjuntos de dados
    private ConjuntoDados conjuntoTreino;
    private ConjuntoDados conjuntoTeste;
    private File arquivoTreinoSelecionado;
    private File arquivoTesteSelecionado;
    private File arquivoUnicoSelecionado;

    // Rede Neural e Treinador
    private RedeNeuralMLP redeNeural;
    private TreinadorMLP treinador;
    private Thread threadTreinamento;
    private MatrizConfusao matrizConfusaoAtual;

    // Componentes da Aba 1 (Dados)
    private RadioButton radioDoisArquivos;
    private RadioButton radioArquivoUnico;
    private Label rotuloArquivoTreino;
    private Label rotuloArquivoTeste;
    private Label rotuloArquivoUnico;
    private Button botaoSelecionarTreino;
    private Button botaoSelecionarTeste;
    private Button botaoSelecionarUnico;
    private TextField campoPorcentagemTreino;
    private CheckBox checkSeedDivisao;
    private TextField campoSeedDivisao;
    private Label rotuloQtdAtributos;
    private Label rotuloQtdClasses;
    private Label rotuloQtdTreino;
    private Label rotuloQtdTeste;
    private TableView<ObservableList<String>> tabelaPreviaDados;

    // Componentes da Aba 2 (Treinamento)
    private TextField campoEntradas;
    private TextField campoSaidas;
    private RadioButton radioOcultaAritmetica;
    private RadioButton radioOcultaGeometrica;
    private RadioButton radioOcultaManual;
    private TextField campoOcultas;
    private RadioButton radioLinear;
    private RadioButton radioLogistica;
    private RadioButton radioHiperbolica;
    private TextField campoTaxaAprendizagem;
    private CheckBox checkParadaEpocas;
    private TextField campoMaxEpocas;
    private CheckBox checkParadaErro;
    private TextField campoErroMinimo;
    private CheckBox checkSeedPesos;
    private TextField campoSeedPesos;

    private Button botaoIniciarTreino;
    private Button botaoPausarTreino;
    private Button botaoInterromperTreino;
    private Button botaoResetarTreino;

    private Label kpiEpoca;
    private Label kpiErroMedio;
    private Label kpiTaxaAtual;
    private Label kpiTempo;
    private Label rotuloStatusPeso;
    private PainelVisualizacaoRede painelRede;
    private LineChart<Number, Number> graficoCurvaErro;
    private XYChart.Series<Number, Number> serieErro;
    private TextArea areaLogEventos;

    // Componentes da Aba 3 (Avaliação)
    private Button botaoAvaliarTeste;
    private Label kpiAcuracia;
    private Label rotuloResumoAcertos;
    private Label rotuloMotivoParada;
    private Label rotuloConfigResumo;
    private PainelMatrizConfusao painelMatrizConfusao;
    private TableView<MetricaClasse> tabelaMetricasClasses;
    private Button botaoExportarHistorico;
    private Button botaoExportarMatriz;

    // Controle de Abas
    private TabPane painelAbas;
    private Tab abaDados;
    private Tab abaTreinamento;
    private Tab abaAvaliacao;

    public TelaPrincipal(Stage palco) {
        this.palcoPrincipal = palco;
        this.conjuntoTreino = new ConjuntoDados();
        this.conjuntoTeste = new ConjuntoDados();

        this.construirInterface();
        this.configurarAcoes();
    }

    /**
     * Monta os elementos visuais principais da aplicação.
     */
    private void construirInterface() {
        // Cabeçalho superior
        HBox cabecalho = new HBox(16);
        cabecalho.getStyleClass().add("barra-cabecalho");
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        VBox titulos = new VBox(2);
        Label titulo = new Label("BackPropagationFX");
        titulo.getStyleClass().add("titulo-aplicacao");
        titulos.getChildren().add(titulo);

        Region espacoFlexivel = new Region();
        HBox.setHgrow(espacoFlexivel, Priority.ALWAYS);

        Label badgeStatus = new Label("Pronto para Configuração");
        badgeStatus.getStyleClass().add("badge-status");

        cabecalho.getChildren().addAll(titulos, espacoFlexivel, badgeStatus);
        this.setTop(cabecalho);

        // Abas centrais
        this.painelAbas = new TabPane();
        this.painelAbas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.abaDados = new Tab("1. Base de Dados", this.criarAbaDados());
        this.abaTreinamento = new Tab("2. Configuração & Treinamento", this.criarAbaTreinamento());
        this.abaAvaliacao = new Tab("3. Avaliação & Resultados", this.criarAbaAvaliacao());

        this.painelAbas.getTabs().addAll(this.abaDados, this.abaTreinamento, this.abaAvaliacao);
        this.setCenter(this.painelAbas);
    }

    /**
     * Cria a visualização da Aba 1: Carregamento de dados.
     */
    private Node criarAbaDados() {
        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(20));

        // Card de Seleção do Modo de Dados
        VBox cardModo = new VBox(12);
        cardModo.getStyleClass().add("card");

        Label tituloCard = new Label("Origem dos Dados");
        tituloCard.getStyleClass().add("card-titulo");

        ToggleGroup grupoModo = new ToggleGroup();
        this.radioDoisArquivos = new RadioButton("Dois arquivos separados (Treinamento e Teste)");
        this.radioDoisArquivos.setToggleGroup(grupoModo);
        this.radioDoisArquivos.setSelected(true);

        this.radioArquivoUnico = new RadioButton("Arquivo único com divisão percentual (Treino % / Teste %)");
        this.radioArquivoUnico.setToggleGroup(grupoModo);

        HBox linhaRadios = new HBox(20);
        linhaRadios.getChildren().addAll(this.radioDoisArquivos, this.radioArquivoUnico);

        // Painel para modo dois arquivos
        GridPane gradeDoisArquivos = new GridPane();
        gradeDoisArquivos.setHgap(10);
        gradeDoisArquivos.setVgap(8);

        this.botaoSelecionarTreino = new Button("Selecionar Base de Treino CSV");
        this.botaoSelecionarTreino.getStyleClass().add("botao-secundario");
        this.rotuloArquivoTreino = new Label("Nenhum arquivo de treino selecionado");
        this.rotuloArquivoTreino.setStyle("-fx-text-fill: #64748b;");

        this.botaoSelecionarTeste = new Button("Selecionar Base de Teste CSV");
        this.botaoSelecionarTeste.getStyleClass().add("botao-secundario");
        this.rotuloArquivoTeste = new Label("Nenhum arquivo de teste selecionado");
        this.rotuloArquivoTeste.setStyle("-fx-text-fill: #64748b;");

        gradeDoisArquivos.add(this.botaoSelecionarTreino, 0, 0);
        gradeDoisArquivos.add(this.rotuloArquivoTreino, 1, 0);
        gradeDoisArquivos.add(this.botaoSelecionarTeste, 0, 1);
        gradeDoisArquivos.add(this.rotuloArquivoTeste, 1, 1);

        // Painel para modo arquivo único
        HBox painelArquivoUnico = new HBox(12);
        painelArquivoUnico.setAlignment(Pos.CENTER_LEFT);
        painelArquivoUnico.setVisible(false);
        painelArquivoUnico.setManaged(false);

        this.botaoSelecionarUnico = new Button("Selecionar Arquivo CSV Completo");
        this.botaoSelecionarUnico.getStyleClass().add("botao-secundario");
        this.rotuloArquivoUnico = new Label("Nenhum arquivo selecionado");
        this.rotuloArquivoUnico.setStyle("-fx-text-fill: #64748b;");

        Label rotuloPorc = new Label("% Treino:");
        this.campoPorcentagemTreino = new TextField("70");
        this.campoPorcentagemTreino.setPrefWidth(55);
        this.campoPorcentagemTreino.getStyleClass().add("campo-texto");

        this.checkSeedDivisao = new CheckBox("Seed:");
        this.campoSeedDivisao = new TextField("42");
        this.campoSeedDivisao.setPrefWidth(65);
        this.campoSeedDivisao.getStyleClass().add("campo-texto");
        this.campoSeedDivisao.setDisable(true);

        painelArquivoUnico.getChildren().addAll(
                this.botaoSelecionarUnico, this.rotuloArquivoUnico,
                rotuloPorc, this.campoPorcentagemTreino,
                this.checkSeedDivisao, this.campoSeedDivisao
        );

        Button botaoCarregar = new Button("Carregar e Processar Dados");
        botaoCarregar.getStyleClass().add("botao-primario");
        botaoCarregar.setOnAction(e -> this.processarCarregamentoDados());

        cardModo.getChildren().addAll(tituloCard, linhaRadios, gradeDoisArquivos, painelArquivoUnico, botaoCarregar);

        // Alternância visual entre os modos
        this.radioDoisArquivos.setOnAction(e -> {
            gradeDoisArquivos.setVisible(true);
            gradeDoisArquivos.setManaged(true);
            painelArquivoUnico.setVisible(false);
            painelArquivoUnico.setManaged(false);
        });

        this.radioArquivoUnico.setOnAction(e -> {
            gradeDoisArquivos.setVisible(false);
            gradeDoisArquivos.setManaged(false);
            painelArquivoUnico.setVisible(true);
            painelArquivoUnico.setManaged(true);
        });

        this.checkSeedDivisao.setOnAction(e -> {
            boolean ativa = this.checkSeedDivisao.isSelected();
            this.campoSeedDivisao.setDisable(!ativa);
        });

        // Cards de Resumo dos Dados
        HBox linhaKpis = new HBox(12);

        VBox kpiCard1 = new VBox(4);
        kpiCard1.getStyleClass().add("kpi-card");
        Label l1 = new Label("ATRIBUTOS DE ENTRADA");
        l1.getStyleClass().add("kpi-rotulo");
        this.rotuloQtdAtributos = new Label("-");
        this.rotuloQtdAtributos.getStyleClass().add("kpi-valor");
        kpiCard1.getChildren().addAll(l1, this.rotuloQtdAtributos);
        HBox.setHgrow(kpiCard1, Priority.ALWAYS);

        VBox kpiCard2 = new VBox(4);
        kpiCard2.getStyleClass().add("kpi-card");
        Label l2 = new Label("CLASSES DE SAÍDA");
        l2.getStyleClass().add("kpi-rotulo");
        this.rotuloQtdClasses = new Label("-");
        this.rotuloQtdClasses.getStyleClass().add("kpi-valor");
        kpiCard2.getChildren().addAll(l2, this.rotuloQtdClasses);
        HBox.setHgrow(kpiCard2, Priority.ALWAYS);

        VBox kpiCard3 = new VBox(4);
        kpiCard3.getStyleClass().add("kpi-card");
        Label l3 = new Label("EXEMPLOS DE TREINO");
        l3.getStyleClass().add("kpi-rotulo");
        this.rotuloQtdTreino = new Label("-");
        this.rotuloQtdTreino.getStyleClass().add("kpi-valor");
        kpiCard3.getChildren().addAll(l3, this.rotuloQtdTreino);
        HBox.setHgrow(kpiCard3, Priority.ALWAYS);

        VBox kpiCard4 = new VBox(4);
        kpiCard4.getStyleClass().add("kpi-card");
        Label l4 = new Label("EXEMPLOS DE TESTE");
        l4.getStyleClass().add("kpi-rotulo");
        this.rotuloQtdTeste = new Label("-");
        this.rotuloQtdTeste.getStyleClass().add("kpi-valor");
        kpiCard4.getChildren().addAll(l4, this.rotuloQtdTeste);
        HBox.setHgrow(kpiCard4, Priority.ALWAYS);

        linhaKpis.getChildren().addAll(kpiCard1, kpiCard2, kpiCard3, kpiCard4);

        // Tabela de Prévia dos Dados
        VBox cardTabela = new VBox(8);
        cardTabela.getStyleClass().add("card");
        VBox.setVgrow(cardTabela, Priority.ALWAYS);

        Label tituloTabela = new Label("Prévia dos Dados de Treino");
        tituloTabela.getStyleClass().add("card-titulo");

        this.tabelaPreviaDados = new TableView<ObservableList<String>>();
        this.tabelaPreviaDados.setPrefHeight(260);
        VBox.setVgrow(this.tabelaPreviaDados, Priority.ALWAYS);

        cardTabela.getChildren().addAll(tituloTabela, this.tabelaPreviaDados);

        conteudo.getChildren().addAll(cardModo, linhaKpis, cardTabela);

        ScrollPane rolagem = new ScrollPane(conteudo);
        rolagem.setFitToWidth(true);
        return rolagem;
    }

    /**
     * Cria a visualização da Aba 2: Configuração da Rede e Treinamento em Tempo Real.
     */
    private Node criarAbaTreinamento() {
        HBox layoutPrincipal = new HBox(16);
        layoutPrincipal.setPadding(new Insets(16));

        // Coluna Esquerda: Configurações
        VBox colunaConfig = new VBox(12);
        colunaConfig.setPrefWidth(320);
        colunaConfig.setMinWidth(300);

        // Card Arquitetura
        VBox cardArq = new VBox(8);
        cardArq.getStyleClass().add("card");

        Label titArq = new Label("Arquitetura da Rede");
        titArq.getStyleClass().add("card-titulo");

        GridPane gradeArq = new GridPane();
        gradeArq.setHgap(8);
        gradeArq.setVgap(8);

        Label lEnt = new Label("Entradas:");
        this.campoEntradas = new TextField("6");
        this.campoEntradas.setDisable(true);
        this.campoEntradas.getStyleClass().add("campo-texto");

        Label lSai = new Label("Saídas:");
        this.campoSaidas = new TextField("5");
        this.campoSaidas.setDisable(true);
        this.campoSaidas.getStyleClass().add("campo-texto");

        gradeArq.add(lEnt, 0, 0);
        gradeArq.add(this.campoEntradas, 1, 0);
        gradeArq.add(lSai, 0, 1);
        gradeArq.add(this.campoSaidas, 1, 1);

        Label lOculta = new Label("Cálculo da Camada Oculta:");
        lOculta.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        ToggleGroup grupoOculta = new ToggleGroup();
        this.radioOcultaAritmetica = new RadioButton("Média Aritmética: (E + S) / 2");
        this.radioOcultaAritmetica.setToggleGroup(grupoOculta);
        this.radioOcultaAritmetica.setSelected(true);

        this.radioOcultaGeometrica = new RadioButton("Média Geométrica: √(E × S)");
        this.radioOcultaGeometrica.setToggleGroup(grupoOculta);

        this.radioOcultaManual = new RadioButton("Definição Manual");
        this.radioOcultaManual.setToggleGroup(grupoOculta);

        HBox linhaOculta = new HBox(8);
        linhaOculta.setAlignment(Pos.CENTER_LEFT);
        Label lQtdOc = new Label("Neurônios Ocultos:");
        this.campoOcultas = new TextField("6");
        this.campoOcultas.setDisable(true);
        this.campoOcultas.setPrefWidth(60);
        this.campoOcultas.getStyleClass().add("campo-texto");
        linhaOculta.getChildren().addAll(lQtdOc, this.campoOcultas);

        cardArq.getChildren().addAll(titArq, gradeArq, lOculta, this.radioOcultaAritmetica, this.radioOcultaGeometrica, this.radioOcultaManual, linhaOculta);

        // Card Função de Transferência
        VBox cardFunc = new VBox(8);
        cardFunc.getStyleClass().add("card");

        Label titFunc = new Label("Função de Transferência");
        titFunc.getStyleClass().add("card-titulo");

        ToggleGroup grupoFunc = new ToggleGroup();
        this.radioLinear = new RadioButton("Linear ( net / 10 )");
        this.radioLinear.setToggleGroup(grupoFunc);

        this.radioLogistica = new RadioButton("Logística Sigmoide ( 0 a 1 )");
        this.radioLogistica.setToggleGroup(grupoFunc);
        this.radioLogistica.setSelected(true);

        this.radioHiperbolica = new RadioButton("Tangente Hiperbólica ( -1 a 1 )");
        this.radioHiperbolica.setToggleGroup(grupoFunc);

        cardFunc.getChildren().addAll(titFunc, this.radioLinear, this.radioLogistica, this.radioHiperbolica);

        // Card Parâmetros e Critérios de Parada
        VBox cardParams = new VBox(8);
        cardParams.getStyleClass().add("card");

        Label titParams = new Label("Parâmetros do Treinamento");
        titParams.getStyleClass().add("card-titulo");

        Label lTaxa = new Label("Taxa de Aprendizagem (η):");
        lTaxa.setStyle("-fx-font-weight: bold;");
        this.campoTaxaAprendizagem = new TextField("0.200000");
        this.campoTaxaAprendizagem.getStyleClass().add("campo-texto");
        Label dicaTaxa = new Label("Valor decimal preciso entre 0 (exclusivo) e 1.");
        dicaTaxa.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        Label titCrit = new Label("Critérios de Parada:");
        titCrit.setStyle("-fx-font-weight: bold;");

        HBox linhaEpocas = new HBox(8);
        linhaEpocas.setAlignment(Pos.CENTER_LEFT);
        this.checkParadaEpocas = new CheckBox("Máx. Épocas:");
        this.checkParadaEpocas.setSelected(true);
        this.campoMaxEpocas = new TextField("2000");
        this.campoMaxEpocas.setPrefWidth(75);
        this.campoMaxEpocas.getStyleClass().add("campo-texto");
        linhaEpocas.getChildren().addAll(this.checkParadaEpocas, this.campoMaxEpocas);

        this.checkParadaEpocas.setOnAction(e -> {
            boolean ativa = this.checkParadaEpocas.isSelected();
            this.campoMaxEpocas.setDisable(!ativa);
            this.atualizarRotuloKpiEpocaInicial();
        });

        HBox linhaErro = new HBox(8);
        linhaErro.setAlignment(Pos.CENTER_LEFT);
        this.checkParadaErro = new CheckBox("Erro Mínimo:");
        this.checkParadaErro.setSelected(true);
        this.campoErroMinimo = new TextField("0.000010");
        this.campoErroMinimo.setPrefWidth(95);
        this.campoErroMinimo.getStyleClass().add("campo-texto");
        linhaErro.getChildren().addAll(this.checkParadaErro, this.campoErroMinimo);

        this.checkParadaErro.setOnAction(e -> {
            boolean ativa = this.checkParadaErro.isSelected();
            this.campoErroMinimo.setDisable(!ativa);
        });

        this.campoMaxEpocas.textProperty().addListener((obs, antigo, novo) -> {
            this.atualizarRotuloKpiEpocaInicial();
        });

        HBox linhaSeed = new HBox(8);
        linhaSeed.setAlignment(Pos.CENTER_LEFT);
        this.checkSeedPesos = new CheckBox("Seed Pesos:");
        this.campoSeedPesos = new TextField("42");
        this.campoSeedPesos.setPrefWidth(75);
        this.campoSeedPesos.setDisable(true);
        this.campoSeedPesos.getStyleClass().add("campo-texto");
        linhaSeed.getChildren().addAll(this.checkSeedPesos, this.campoSeedPesos);

        this.checkSeedPesos.setOnAction(e -> {
            boolean ativa = this.checkSeedPesos.isSelected();
            this.campoSeedPesos.setDisable(!ativa);
        });

        cardParams.getChildren().addAll(titParams, lTaxa, this.campoTaxaAprendizagem, dicaTaxa, titCrit, linhaEpocas, linhaErro, linhaSeed);

        // Botões de Ação
        VBox cardBotoes = new VBox(8);
        cardBotoes.getStyleClass().add("card");

        this.botaoIniciarTreino = new Button("▶ Iniciar Treinamento");
        this.botaoIniciarTreino.getStyleClass().add("botao-primario");
        this.botaoIniciarTreino.setMaxWidth(Double.MAX_VALUE);

        this.botaoPausarTreino = new Button("⏸ Pausar");
        this.botaoPausarTreino.getStyleClass().add("botao-secundario");
        this.botaoPausarTreino.setMaxWidth(Double.MAX_VALUE);
        this.botaoPausarTreino.setDisable(true);

        this.botaoInterromperTreino = new Button("⏹ Interromper");
        this.botaoInterromperTreino.getStyleClass().add("botao-perigo");
        this.botaoInterromperTreino.setMaxWidth(Double.MAX_VALUE);
        this.botaoInterromperTreino.setDisable(true);

        this.botaoResetarTreino = new Button("🔄 Resetar Treinamento");
        this.botaoResetarTreino.getStyleClass().add("botao-secundario");
        this.botaoResetarTreino.setMaxWidth(Double.MAX_VALUE);

        cardBotoes.getChildren().addAll(this.botaoIniciarTreino, this.botaoPausarTreino, this.botaoInterromperTreino, this.botaoResetarTreino);

        colunaConfig.getChildren().addAll(cardArq, cardFunc, cardParams, cardBotoes);

        // Coluna Direita: Monitoramento e Visualizações
        VBox colunaVisual = new VBox(12);
        HBox.setHgrow(colunaVisual, Priority.ALWAYS);

        // KPIs em tempo real
        HBox kpiLinha = new HBox(12);

        VBox kpiCard1 = new VBox(4);
        kpiCard1.getStyleClass().add("kpi-card");
        Label lk1 = new Label("ÉPOCA");
        lk1.getStyleClass().add("kpi-rotulo");
        this.kpiEpoca = new Label("0 / 0");
        this.kpiEpoca.getStyleClass().add("kpi-valor");
        kpiCard1.getChildren().addAll(lk1, this.kpiEpoca);
        HBox.setHgrow(kpiCard1, Priority.ALWAYS);

        VBox kpiCard2 = new VBox(4);
        kpiCard2.getStyleClass().add("kpi-card");
        Label lk2 = new Label("ERRO MÉDIO");
        lk2.getStyleClass().add("kpi-rotulo");
        this.kpiErroMedio = new Label("0.000000");
        this.kpiErroMedio.getStyleClass().add("kpi-valor");
        kpiCard2.getChildren().addAll(lk2, this.kpiErroMedio);
        HBox.setHgrow(kpiCard2, Priority.ALWAYS);

        VBox kpiCard3 = new VBox(4);
        kpiCard3.getStyleClass().add("kpi-card");
        Label lk3 = new Label("η ATUAL");
        lk3.getStyleClass().add("kpi-rotulo");
        this.kpiTaxaAtual = new Label("0.200000");
        this.kpiTaxaAtual.getStyleClass().add("kpi-valor");
        kpiCard3.getChildren().addAll(lk3, this.kpiTaxaAtual);
        HBox.setHgrow(kpiCard3, Priority.ALWAYS);

        VBox kpiCard4 = new VBox(4);
        kpiCard4.getStyleClass().add("kpi-card");
        Label lk4 = new Label("TEMPO");
        lk4.getStyleClass().add("kpi-rotulo");
        this.kpiTempo = new Label("00:00");
        this.kpiTempo.getStyleClass().add("kpi-valor");
        kpiCard4.getChildren().addAll(lk4, this.kpiTempo);
        HBox.setHgrow(kpiCard4, Priority.ALWAYS);

        kpiLinha.getChildren().addAll(kpiCard1, kpiCard2, kpiCard3, kpiCard4);

        // Painel Central com Arquitetura e Gráfico
        HBox paineisGraficos = new HBox(12);
        VBox.setVgrow(paineisGraficos, Priority.ALWAYS);

        // Visualização da Rede
        VBox boxRede = new VBox(8);
        boxRede.getStyleClass().add("card");
        HBox.setHgrow(boxRede, Priority.ALWAYS);

        Label titRede = new Label("Arquitetura da Rede");
        titRede.getStyleClass().add("card-titulo");

        this.rotuloStatusPeso = new Label("Passe o mouse sobre uma conexão");
        this.rotuloStatusPeso.setStyle("-fx-font-size: 11px; -fx-text-fill: #006729; -fx-font-weight: bold;");

        this.painelRede = new PainelVisualizacaoRede(this.rotuloStatusPeso);
        this.painelRede.setPrefHeight(280);
        VBox.setVgrow(this.painelRede, Priority.ALWAYS);

        boxRede.getChildren().addAll(titRede, this.rotuloStatusPeso, this.painelRede);

        // Gráfico da Curva de Erro
        VBox boxGrafico = new VBox(8);
        boxGrafico.getStyleClass().add("card");
        HBox.setHgrow(boxGrafico, Priority.ALWAYS);

        Label titGrafico = new Label("Curva de Convergência do Erro Médio por Época");
        titGrafico.getStyleClass().add("card-titulo");

        NumberAxis eixoX = new NumberAxis();
        eixoX.setLabel("Épocas");
        NumberAxis eixoY = new NumberAxis();
        eixoY.setLabel("Erro Médio");

        this.graficoCurvaErro = new LineChart<Number, Number>(eixoX, eixoY);
        this.graficoCurvaErro.setCreateSymbols(false);
        this.graficoCurvaErro.setAnimated(false);
        this.graficoCurvaErro.setLegendVisible(false);

        this.serieErro = new XYChart.Series<Number, Number>();
        this.graficoCurvaErro.getData().add(this.serieErro);
        VBox.setVgrow(this.graficoCurvaErro, Priority.ALWAYS);

        boxGrafico.getChildren().addAll(titGrafico, this.graficoCurvaErro);

        paineisGraficos.getChildren().addAll(boxRede, boxGrafico);

        // Log de Eventos e Platô
        VBox cardLog = new VBox(6);
        cardLog.getStyleClass().add("card");
        cardLog.setPrefHeight(130);

        Label titLog = new Label("Registro de Eventos (Platô, Reduções de η e Critérios de Parada)");
        titLog.getStyleClass().add("card-titulo");

        this.areaLogEventos = new TextArea();
        this.areaLogEventos.setEditable(false);
        this.areaLogEventos.getStyleClass().add("area-log");
        this.areaLogEventos.setPrefHeight(90);

        cardLog.getChildren().addAll(titLog, this.areaLogEventos);

        colunaVisual.getChildren().addAll(kpiLinha, paineisGraficos, cardLog);

        layoutPrincipal.getChildren().addAll(colunaConfig, colunaVisual);

        ScrollPane rolagem = new ScrollPane(layoutPrincipal);
        rolagem.setFitToWidth(true);
        rolagem.setFitToHeight(true);
        return rolagem;
    }

    /**
     * Cria a visualização da Aba 3: Avaliação na Base de Teste e Matriz de Confusão.
     */
    private Node criarAbaAvaliacao() {
        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(20));

        // Barra Superior de Ação
        HBox barraAcao = new HBox(16);
        barraAcao.getStyleClass().add("card");
        barraAcao.setAlignment(Pos.CENTER_LEFT);

        this.botaoAvaliarTeste = new Button("🧪 Avaliar Modelo na Base de Teste");
        this.botaoAvaliarTeste.getStyleClass().add("botao-sucesso");
        this.botaoAvaliarTeste.setDisable(true);

        this.botaoExportarHistorico = new Button("📥 Exportar Histórico do Erro (CSV)");
        this.botaoExportarHistorico.getStyleClass().add("botao-secundario");
        this.botaoExportarHistorico.setDisable(true);

        this.botaoExportarMatriz = new Button("📥 Exportar Matriz de Confusão (CSV)");
        this.botaoExportarMatriz.getStyleClass().add("botao-secundario");
        this.botaoExportarMatriz.setDisable(true);

        barraAcao.getChildren().addAll(this.botaoAvaliarTeste, this.botaoExportarHistorico, this.botaoExportarMatriz);

        // Cards de Resumo da Avaliação
        HBox linhaResultados = new HBox(12);

        VBox cardAcuracia = new VBox(6);
        cardAcuracia.getStyleClass().add("card");
        cardAcuracia.setAlignment(Pos.CENTER);
        cardAcuracia.setPrefWidth(220);

        Label titAc = new Label("ACURÁCIA NO TESTE");
        titAc.getStyleClass().add("kpi-rotulo");

        this.kpiAcuracia = new Label("- %");
        this.kpiAcuracia.getStyleClass().add("kpi-destaque");

        this.rotuloResumoAcertos = new Label("Aguardando teste");
        this.rotuloResumoAcertos.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        cardAcuracia.getChildren().addAll(titAc, this.kpiAcuracia, this.rotuloResumoAcertos);

        VBox cardConfig = new VBox(6);
        cardConfig.getStyleClass().add("card");
        HBox.setHgrow(cardConfig, Priority.ALWAYS);

        Label titConf = new Label("Resumo do Treinamento");
        titConf.getStyleClass().add("card-titulo");

        this.rotuloMotivoParada = new Label("Critério de Parada: Nenhum treinamento realizado.");
        this.rotuloMotivoParada.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        this.rotuloConfigResumo = new Label("Configuração: -");
        this.rotuloConfigResumo.setStyle("-fx-text-fill: #475569;");

        cardConfig.getChildren().addAll(titConf, this.rotuloMotivoParada, this.rotuloConfigResumo);

        linhaResultados.getChildren().addAll(cardAcuracia, cardConfig);

        // Painel com a Matriz de Confusão e Tabela de Métricas
        HBox linhaMatriz = new HBox(16);
        VBox.setVgrow(linhaMatriz, Priority.ALWAYS);

        // Card da Matriz de Confusão
        VBox cardMatriz = new VBox(10);
        cardMatriz.getStyleClass().add("card");
        HBox.setHgrow(cardMatriz, Priority.ALWAYS);

        Label titMatriz = new Label("Matriz de Confusão (Linhas: Classe Real, Colunas: Classe Prevista)");
        titMatriz.getStyleClass().add("card-titulo");

        this.painelMatrizConfusao = new PainelMatrizConfusao();
        VBox.setVgrow(this.painelMatrizConfusao, Priority.ALWAYS);

        cardMatriz.getChildren().addAll(titMatriz, this.painelMatrizConfusao);

        // Card da Tabela de Desempenho por Classe (Precisão e Revocação)
        VBox cardDesempenho = new VBox(10);
        cardDesempenho.getStyleClass().add("card");
        cardDesempenho.setPrefWidth(380);

        Label titDesemp = new Label("Desempenho Individual por Classe");
        titDesemp.getStyleClass().add("card-titulo");

        this.tabelaMetricasClasses = new TableView<MetricaClasse>();
        this.configurarTabelaMetricas();
        VBox.setVgrow(this.tabelaMetricasClasses, Priority.ALWAYS);

        cardDesempenho.getChildren().addAll(titDesemp, this.tabelaMetricasClasses);

        linhaMatriz.getChildren().addAll(cardMatriz, cardDesempenho);

        conteudo.getChildren().addAll(barraAcao, linhaResultados, linhaMatriz);

        ScrollPane rolagem = new ScrollPane(conteudo);
        rolagem.setFitToWidth(true);
        return rolagem;
    }

    /**
     * Configura as colunas da tabela de Precisão e Revocação por classe.
     */
    private void configurarTabelaMetricas() {
        TableColumn<MetricaClasse, String> colClasse = new TableColumn<MetricaClasse, String>("Classe");
        colClasse.setCellValueFactory(cellData -> cellData.getValue().classeProperty());
        colClasse.setPrefWidth(65);

        TableColumn<MetricaClasse, String> colTotal = new TableColumn<MetricaClasse, String>("Exemplos");
        colTotal.setCellValueFactory(cellData -> cellData.getValue().exemplosProperty());
        colTotal.setPrefWidth(70);

        TableColumn<MetricaClasse, String> colAcertos = new TableColumn<MetricaClasse, String>("Acertos");
        colAcertos.setCellValueFactory(cellData -> cellData.getValue().acertosProperty());
        colAcertos.setPrefWidth(65);

        TableColumn<MetricaClasse, String> colPrecisao = new TableColumn<MetricaClasse, String>("Precisão");
        colPrecisao.setCellValueFactory(cellData -> cellData.getValue().precisaoProperty());
        colPrecisao.setPrefWidth(85);

        TableColumn<MetricaClasse, String> colRevocacao = new TableColumn<MetricaClasse, String>("Revocação");
        colRevocacao.setCellValueFactory(cellData -> cellData.getValue().revocacaoProperty());
        colRevocacao.setPrefWidth(85);

        this.tabelaMetricasClasses.getColumns().addAll(colClasse, colTotal, colAcertos, colPrecisao, colRevocacao);
    }

    /**
     * Associa os eventos aos botões e controles da interface.
     */
    private void configurarAcoes() {
        // Seleção de arquivos
        this.botaoSelecionarTreino.setOnAction(e -> this.selecionarArquivoTreino());
        this.botaoSelecionarTeste.setOnAction(e -> this.selecionarArquivoTeste());
        this.botaoSelecionarUnico.setOnAction(e -> this.selecionarArquivoUnico());

        // Atualização da camada oculta
        this.radioOcultaAritmetica.setOnAction(e -> this.atualizarCalculoOculta());
        this.radioOcultaGeometrica.setOnAction(e -> this.atualizarCalculoOculta());
        this.radioOcultaManual.setOnAction(e -> {
            this.campoOcultas.setDisable(false);
            this.campoOcultas.requestFocus();
        });

        // Controle de Treinamento
        this.botaoIniciarTreino.setOnAction(e -> this.iniciarTreinamento());
        this.botaoPausarTreino.setOnAction(e -> this.pausarOuRetomarTreinamento());
        this.botaoInterromperTreino.setOnAction(e -> this.interromperTreinamento());
        this.botaoResetarTreino.setOnAction(e -> this.resetarTreinamento());

        // Avaliação e Exportação
        this.botaoAvaliarTeste.setOnAction(e -> this.executarAvaliacaoTeste());
        this.botaoExportarHistorico.setOnAction(e -> this.exportarHistoricoParaCsv());
        this.botaoExportarMatriz.setOnAction(e -> this.exportarMatrizParaCsv());
    }

    private void selecionarArquivoTreino() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Selecionar Arquivo CSV de Treinamento");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv"));
        File arquivo = seletor.showOpenDialog(this.palcoPrincipal);
        if (arquivo != null) {
            this.arquivoTreinoSelecionado = arquivo;
            this.rotuloArquivoTreino.setText(arquivo.getName());
        }
    }

    private void selecionarArquivoTeste() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Selecionar Arquivo CSV de Teste");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv"));
        File arquivo = seletor.showOpenDialog(this.palcoPrincipal);
        if (arquivo != null) {
            this.arquivoTesteSelecionado = arquivo;
            this.rotuloArquivoTeste.setText(arquivo.getName());
        }
    }

    private void selecionarArquivoUnico() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Selecionar Arquivo CSV Completo");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv"));
        File arquivo = seletor.showOpenDialog(this.palcoPrincipal);
        if (arquivo != null) {
            this.arquivoUnicoSelecionado = arquivo;
            this.rotuloArquivoUnico.setText(arquivo.getName());
        }
    }

    /**
     * Processa os arquivos CSV selecionados de acordo com o modo escolhido.
     */
    private void processarCarregamentoDados() {
        boolean sucesso = true;

        if (this.radioDoisArquivos.isSelected()) {
            if (this.arquivoTreinoSelecionado == null || this.arquivoTesteSelecionado == null) {
                this.exibirAlerta("Atenção", "Por favor, selecione os dois arquivos CSV (Treino e Teste).", Alert.AlertType.WARNING);
                sucesso = false;
            } else {
                boolean leuTreino = this.conjuntoTreino.carregarArquivoCsv(this.arquivoTreinoSelecionado);
                boolean leuTeste = this.conjuntoTeste.carregarArquivoCsv(this.arquivoTesteSelecionado);

                if (!leuTreino || !leuTeste) {
                    this.exibirAlerta("Erro", "Falha ao ler os arquivos CSV informados. Verifique a formatação.", Alert.AlertType.ERROR);
                    sucesso = false;
                }
            }
        } else {
            if (this.arquivoUnicoSelecionado == null) {
                this.exibirAlerta("Atenção", "Por favor, selecione um arquivo CSV completo.", Alert.AlertType.WARNING);
                sucesso = false;
            } else {
                ConjuntoDados baseCompleta = new ConjuntoDados();
                boolean leu = baseCompleta.carregarArquivoCsv(this.arquivoUnicoSelecionado);
                if (!leu) {
                    this.exibirAlerta("Erro", "Falha ao ler o arquivo CSV informado.", Alert.AlertType.ERROR);
                    sucesso = false;
                } else {
                    double porcTreino = 70.0;
                    try {
                        porcTreino = Double.parseDouble(this.campoPorcentagemTreino.getText().trim());
                    } catch (Exception ex) {
                        porcTreino = 70.0;
                    }

                    boolean usarSeed = this.checkSeedDivisao.isSelected();
                    long seed = 42;
                    if (usarSeed) {
                        try {
                            seed = Long.parseLong(this.campoSeedDivisao.getText().trim());
                        } catch (Exception ex) {
                            seed = 42;
                        }
                    }

                    ConjuntoDados[] divisao = baseCompleta.dividirTreinoETeste(porcTreino, usarSeed, seed);
                    this.conjuntoTreino = divisao[0];
                    this.conjuntoTeste = divisao[1];
                }
            }
        }

        if (sucesso) {
            // RF03: Calcula os valores mínimo e máximo de cada atributo somente no treino
            this.conjuntoTreino.calcularMinimosEMaximos();

            // Normaliza o treino e o teste utilizando estritamente os min e max do treino
            double[] min = this.conjuntoTreino.getMenoresValores();
            double[] max = this.conjuntoTreino.getMaioresValores();
            this.conjuntoTreino.normalizar(min, max);
            this.conjuntoTeste.normalizar(min, max);

            // Determina a função selecionada para codificar o vetor one-hot
            int tipoFuncao = this.obterTipoFuncaoSelecionada();
            this.conjuntoTreino.codificarSaidasOneHot(tipoFuncao, this.conjuntoTreino.getClassesUnicas());
            this.conjuntoTeste.codificarSaidasOneHot(tipoFuncao, this.conjuntoTreino.getClassesUnicas());

            // Atualiza indicadores visuais
            int qtdEntradas = this.conjuntoTreino.getQuantidadeAtributos();
            int qtdClasses = this.conjuntoTreino.getQuantidadeClasses();
            int totalTreino = this.conjuntoTreino.getQuantidadeExemplos();
            int totalTeste = this.conjuntoTeste.getQuantidadeExemplos();

            this.rotuloQtdAtributos.setText(String.valueOf(qtdEntradas));
            this.rotuloQtdClasses.setText(String.valueOf(qtdClasses));
            this.rotuloQtdTreino.setText(String.valueOf(totalTreino));
            this.rotuloQtdTeste.setText(String.valueOf(totalTeste));

            this.campoEntradas.setText(String.valueOf(qtdEntradas));
            this.campoSaidas.setText(String.valueOf(qtdClasses));

            this.atualizarCalculoOculta();
            this.preencherTabelaPrevia();

            this.inicializarEstruturaRedeVisual();

            this.exibirAlerta("Sucesso", "Base de dados carregada, normalizada e codificada com sucesso!\n" +
                    "Entradas: " + qtdEntradas + " | Classes: " + qtdClasses + "\n" +
                    "Treino: " + totalTreino + " linhas | Teste: " + totalTeste + " linhas.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Preenche a tabela com as primeiras linhas dos dados normalizados e one-hot.
     */
    private void preencherTabelaPrevia() {
        this.tabelaPreviaDados.getColumns().clear();
        this.tabelaPreviaDados.getItems().clear();

        ArrayList<String> nomesAtributos = this.conjuntoTreino.getNomesAtributos();
        int qtdAtributos = nomesAtributos.size();

        for (int i = 0; i < qtdAtributos; i = i + 1) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> coluna = new TableColumn<ObservableList<String>, String>(nomesAtributos.get(i));
            coluna.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));
            this.tabelaPreviaDados.getColumns().add(coluna);
        }

        // Coluna da classe
        final int colClasseIndex = qtdAtributos;
        TableColumn<ObservableList<String>, String> colClasse = new TableColumn<ObservableList<String>, String>("Classe");
        colClasse.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colClasseIndex)));
        this.tabelaPreviaDados.getColumns().add(colClasse);

        // Coluna da saída desejada
        final int colOneHotIndex = qtdAtributos + 1;
        TableColumn<ObservableList<String>, String> colOneHot = new TableColumn<ObservableList<String>, String>("Saída Desejada");
        colOneHot.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colOneHotIndex)));
        this.tabelaPreviaDados.getColumns().add(colOneHot);

        // Adiciona as linhas (até 100 linhas para prévia rápida)
        ArrayList<ExemploDado> exemplos = this.conjuntoTreino.getExemplos();
        int limite = exemplos.size();
        if (limite > 100) {
            limite = 100;
        }

        for (int lin = 0; lin < limite; lin = lin + 1) {
            ExemploDado ex = exemplos.get(lin);
            ObservableList<String> linha = FXCollections.observableArrayList();

            double[] norm = ex.getAtributosNormalizados();
            for (int col = 0; col < qtdAtributos; col = col + 1) {
                linha.add(String.format("%.4f", norm[col]));
            }

            linha.add(ex.getRotuloClasse());

            // Formata vetor one-hot
            double[] oneHot = ex.getVetorDesejado();
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < oneHot.length; k = k + 1) {
                sb.append(String.format("%.0f ", oneHot[k]));
            }
            linha.add(sb.toString().trim());

            this.tabelaPreviaDados.getItems().add(linha);
        }
    }

    /**
     * Atualiza o cálculo do número de neurônios ocultos com base nas heurísticas:
     * - Aritmética: (Entradas + Saídas) / 2
     * - Geométrica: raiz quadrada de (Entradas * Saídas)
     */
    private void atualizarCalculoOculta() {
        int e = 0;
        int s = 0;
        try {
            e = Integer.parseInt(this.campoEntradas.getText().trim());
            s = Integer.parseInt(this.campoSaidas.getText().trim());
        } catch (Exception ex) {
            e = 6;
            s = 5;
        }

        int valorCalculado = 6;

        if (this.radioOcultaAritmetica.isSelected()) {
            this.campoOcultas.setDisable(true);
            double mediaAritmetica = (double) (e + s) / 2.0;
            valorCalculado = (int) Math.round(mediaAritmetica);
            this.campoOcultas.setText(String.valueOf(valorCalculado));
        } else {
            if (this.radioOcultaGeometrica.isSelected()) {
                this.campoOcultas.setDisable(true);
                double mediaGeometrica = Math.sqrt((double) (e * s));
                valorCalculado = (int) Math.round(mediaGeometrica);
                this.campoOcultas.setText(String.valueOf(valorCalculado));
            }
        }

        this.inicializarEstruturaRedeVisual();
    }

    private int obterTipoFuncaoSelecionada() {
        int tipo = 2; // Padrão: Logística
        if (this.radioLinear.isSelected()) {
            tipo = 1;
        } else {
            if (this.radioHiperbolica.isSelected()) {
                tipo = 3;
            }
        }
        return tipo;
    }

    /**
     * Inicializa a rede neural em memória e desenha sua topologia no painel.
     */
    private void inicializarEstruturaRedeVisual() {
        int e = 0;
        int o = 0;
        int s = 0;

        try {
            e = Integer.parseInt(this.campoEntradas.getText().trim());
            o = Integer.parseInt(this.campoOcultas.getText().trim());
            s = Integer.parseInt(this.campoSaidas.getText().trim());
        } catch (Exception ex) {
            e = 6;
            o = 6;
            s = 5;
        }

        int tipoFuncao = this.obterTipoFuncaoSelecionada();
        this.redeNeural = new RedeNeuralMLP(e, o, s, tipoFuncao);

        boolean usarSeed = this.checkSeedPesos.isSelected();
        long seed = 42;
        if (usarSeed) {
            try {
                seed = Long.parseLong(this.campoSeedPesos.getText().trim());
            } catch (Exception ex) {
                seed = 42;
            }
        }

        this.redeNeural.inicializarPesosAleatorios(usarSeed, seed);

        ArrayList<String> entradas = this.conjuntoTreino.getNomesAtributos();
        ArrayList<String> saidas = this.conjuntoTreino.getClassesUnicas();
        this.painelRede.redesenharRede(this.redeNeural, entradas, saidas);
    }

    /**
     * Inicia a rotina de treinamento com Backpropagation.
     */
    private void iniciarTreinamento() {
        if (this.conjuntoTreino.getQuantidadeExemplos() == 0) {
            this.exibirAlerta("Atenção", "Carregue a base de dados na Aba 1 antes de iniciar o treinamento.", Alert.AlertType.WARNING);
        } else {
            // Validação da taxa de aprendizagem: deve ser 0 < eta <= 1
            double eta = 0.0;
            boolean taxaValida = true;
            try {
                String textoTaxa = this.campoTaxaAprendizagem.getText().trim().replace(',', '.');
                eta = Double.parseDouble(textoTaxa);
                if (eta <= 0.0 || eta > 1.0) {
                    taxaValida = false;
                }
            } catch (Exception ex) {
                taxaValida = false;
            }

            if (!taxaValida) {
                this.exibirAlerta("Taxa Inválida", "A taxa de aprendizagem (η) deve ser um número maior que 0 e menor ou igual a 1 (0 < η ≤ 1).\nExemplo: 0.15 ou 0.01", Alert.AlertType.ERROR);
            } else {
                boolean usarEpocas = this.checkParadaEpocas.isSelected();
                boolean usarErro = this.checkParadaErro.isSelected();

                // Validação de segurança: pelo menos um critério deve estar marcado
                if (!usarEpocas && !usarErro) {
                    this.exibirAlerta("Critério de Parada Obrigatório",
                            "É obrigatório selecionar pelo menos um critério de parada (Máx. Épocas e/ou Erro Mínimo) para evitar execução infinita da rede.",
                            Alert.AlertType.WARNING);
                } else {
                    int maxEpocas = 2000;
                    if (usarEpocas) {
                        try {
                            maxEpocas = Integer.parseInt(this.campoMaxEpocas.getText().trim());
                            if (maxEpocas <= 0) {
                                maxEpocas = 2000;
                            }
                        } catch (Exception ex) {
                            maxEpocas = 2000;
                        }
                    }

                    double erroMin = 0.00001;
                    if (usarErro) {
                        try {
                            String textoErro = this.campoErroMinimo.getText().trim().replace(',', '.');
                            erroMin = Double.parseDouble(textoErro);
                        } catch (Exception ex) {
                            erroMin = 0.00001;
                        }
                    }

                    int tipoFuncao = this.obterTipoFuncaoSelecionada();
                    this.redeNeural.setTipoFuncaoAtivacao(tipoFuncao);

                    // Recodifica as saídas para a função escolhida (caso tenha mudado)
                    this.conjuntoTreino.codificarSaidasOneHot(tipoFuncao, this.conjuntoTreino.getClassesUnicas());
                    this.conjuntoTeste.codificarSaidasOneHot(tipoFuncao, this.conjuntoTreino.getClassesUnicas());

                    // Limpa gráfico e logs
                    this.serieErro.getData().clear();
                    this.areaLogEventos.clear();
                    String infoParada = "";
                    if (usarEpocas) {
                        infoParada = infoParada + " | Máx Épocas: " + maxEpocas;
                    }
                    if (usarErro) {
                        infoParada = infoParada + " | Erro Mín: " + String.format("%.6f", erroMin);
                    }
                    this.adicionarLog("Treinamento iniciado com η = " + String.format("%.6f", eta) + infoParada);

                    // Configura botões de controle
                    this.botaoIniciarTreino.setDisable(true);
                    this.botaoPausarTreino.setDisable(false);
                    this.botaoPausarTreino.setText("⏸ Pausar");
                    this.botaoInterromperTreino.setDisable(false);

                    // Instancia o treinador e inicia em thread separada
                    this.treinador = new TreinadorMLP(
                            this.redeNeural,
                            this.conjuntoTreino,
                            eta,
                            usarEpocas,
                            maxEpocas,
                            usarErro,
                            erroMin,
                            this
                    );

                    this.threadTreinamento = new Thread(this.treinador);
                    this.threadTreinamento.setDaemon(true);
                    this.threadTreinamento.start();
                }
            }
        }
    }

    private void pausarOuRetomarTreinamento() {
        if (this.treinador != null) {
            if (this.treinador.isPausado()) {
                this.treinador.retomar();
                this.botaoPausarTreino.setText("⏸ Pausar");
                this.adicionarLog("Treinamento retomado pelo usuário.");
            } else {
                this.treinador.pausar();
                this.botaoPausarTreino.setText("▶ Retomar");
                this.adicionarLog("Treinamento pausado pelo usuário.");
            }
        }
    }

    private void interromperTreinamento() {
        if (this.treinador != null) {
            this.treinador.interromper();
            this.botaoPausarTreino.setDisable(true);
            this.botaoInterromperTreino.setDisable(true);
            this.adicionarLog("Solicitação de interrupção enviada...");
        }
    }

    /**
     * Reseta completamente o estado de treinamento:
     * - Interrompe qualquer treino em execução;
     * - Reinicializa a rede neural com novos pesos aleatórios (respeitando a seed se selecionada);
     * - Limpa o gráfico da curva de erro;
     * - Limpa a área de logs e os indicadores visuais (KPIs);
     * - Restaura os botões de ação para o estado inicial.
     */
    private void resetarTreinamento() {
        if (this.treinador != null && this.treinador.isExecutando()) {
            this.treinador.interromper();
        }

        // Limpa o gráfico e logs
        this.serieErro.getData().clear();
        this.areaLogEventos.clear();

        // Reinicia a estrutura e os pesos da rede
        this.inicializarEstruturaRedeVisual();

        // Reseta os KPIs na tela
        this.atualizarRotuloKpiEpocaInicial();
        this.kpiErroMedio.setText("0.000000");
        this.kpiTempo.setText("00:00");

        String textoTaxa = this.campoTaxaAprendizagem.getText().trim().replace(',', '.');
        if (textoTaxa.length() > 0) {
            this.kpiTaxaAtual.setText(textoTaxa);
        } else {
            this.kpiTaxaAtual.setText("0.200000");
        }

        // Restaura os botões de controle
        this.botaoIniciarTreino.setDisable(false);
        this.botaoPausarTreino.setDisable(true);
        this.botaoPausarTreino.setText("⏸ Pausar");
        this.botaoInterromperTreino.setDisable(true);

        this.adicionarLog("Treinamento resetado com sucesso. Pesos reinicializados.");
    }

    /**
     * Atualiza o texto do indicador visual da época inicial antes ou durante a configuração.
     */
    private void atualizarRotuloKpiEpocaInicial() {
        if (this.checkParadaEpocas.isSelected()) {
            String textoMax = this.campoMaxEpocas.getText().trim();
            if (textoMax.length() == 0) {
                textoMax = "2000";
            }
            this.kpiEpoca.setText("0 / " + textoMax);
        } else {
            this.kpiEpoca.setText("0");
        }
    }

    /**
     * Atualização dos indicadores e gráfico em tempo real.
     */
    @Override
    public void aoAtualizarProgresso(int epoca, double erroMedio, double taxaAtual, long tempoDecorrido) {
        Platform.runLater(() -> {
            if (this.checkParadaEpocas.isSelected()) {
                int maxEpocas = 2000;
                try {
                    maxEpocas = Integer.parseInt(this.campoMaxEpocas.getText().trim());
                } catch (Exception ex) {
                    maxEpocas = 2000;
                }
                this.kpiEpoca.setText(epoca + " / " + maxEpocas);
            } else {
                this.kpiEpoca.setText(String.valueOf(epoca));
            }

            this.kpiErroMedio.setText(String.format("%.6f", erroMedio));
            this.kpiTaxaAtual.setText(String.format("%.6f", taxaAtual));

            long segundosTotais = tempoDecorrido / 1000;
            long minutos = segundosTotais / 60;
            long segundos = segundosTotais % 60;
            this.kpiTempo.setText(String.format("%02d:%02d", minutos, segundos));

            // Atualiza o gráfico periodicamente para não degradar a taxa de quadros
            if (epoca % 5 == 0 || erroMedio < 0.0001) {
                this.serieErro.getData().add(new XYChart.Data<Number, Number>(epoca, erroMedio));
            }

            // Atualiza a visualização gráfica da rede a cada 50 épocas
            if (epoca % 50 == 0) {
                ArrayList<String> entradas = this.conjuntoTreino.getNomesAtributos();
                ArrayList<String> saidas = this.conjuntoTreino.getClassesUnicas();
                this.painelRede.redesenharRede(this.redeNeural, entradas, saidas);
            }
        });
    }

    /**
     * Detecção de platô conforme Seção 6 do enunciado.
     */
    @Override
    public void aoDetectarPlato(double desvioPadrao, double taxaAtual) {
        Platform.runLater(() -> {
            String mensagemLog = "Platô detectado! Desvio padrão das últimas 10 épocas: " +
                    String.format("%.8f", desvioPadrao) + " | η atual: " + String.format("%.6f", taxaAtual);
            this.adicionarLog(mensagemLog);

            // Abre a caixa de diálogo com as 3 opções exigidas pelo enunciado
            Alert alertaPlato = new Alert(Alert.AlertType.CONFIRMATION);
            alertaPlato.setTitle("Atenção: Platô Detectado!");
            alertaPlato.setHeaderText("O treinamento parece estagnado nas últimas 10 épocas.\n" +
                    "Desvio padrão do erro médio: " + String.format("%.8f", desvioPadrao));
            alertaPlato.setContentText("Escolha a ação desejada:");

            double taxaReduzida = taxaAtual * 0.9;
            ButtonType btnReduzir = new ButtonType("Reduzir η em 10% (" + String.format("%.6f", taxaReduzida) + ")");
            ButtonType btnManter = new ButtonType("Continuar sem alterar η");
            ButtonType btnParar = new ButtonType("Interromper treinamento");

            alertaPlato.getButtonTypes().setAll(btnReduzir, btnManter, btnParar);

            alertaPlato.showAndWait().ifPresent(resposta -> {
                if (resposta == btnReduzir) {
                    this.adicionarLog("Usuário optou por reduzir η em 10%: novo η = " + String.format("%.6f", taxaReduzida));
                    this.treinador.aplicarDecisaoPlato(3);
                    this.botaoPausarTreino.setText("⏸ Pausar");
                } else {
                    if (resposta == btnManter) {
                        this.adicionarLog("Usuário optou por continuar sem alterar η.");
                        this.treinador.aplicarDecisaoPlato(2);
                        this.botaoPausarTreino.setText("⏸ Pausar");
                    } else {
                        this.adicionarLog("Usuário optou por interromper o treinamento devido ao platô.");
                        this.treinador.aplicarDecisaoPlato(1);
                    }
                }
            });
        });
    }

    /**
     * Término do treinamento da rede neural.
     */
    @Override
    public void aoFinalizarTreinamento(String motivoFinalizacao, int epocasExecutadas, double erroFinal) {
        Platform.runLater(() -> {
            this.adicionarLog("Fim do Treinamento: " + motivoFinalizacao);
            this.adicionarLog("Total de épocas: " + epocasExecutadas + " | Erro final: " + String.format("%.6f", erroFinal));

            // Atualização visual final da rede
            ArrayList<String> entradas = this.conjuntoTreino.getNomesAtributos();
            ArrayList<String> saidas = this.conjuntoTreino.getClassesUnicas();
            this.painelRede.redesenharRede(this.redeNeural, entradas, saidas);

            this.botaoIniciarTreino.setDisable(false);
            this.botaoPausarTreino.setDisable(true);
            this.botaoPausarTreino.setText("⏸ Pausar");
            this.botaoInterromperTreino.setDisable(true);

            this.botaoAvaliarTeste.setDisable(false);
            this.botaoExportarHistorico.setDisable(false);

            this.rotuloMotivoParada.setText("Critério de Parada: " + motivoFinalizacao);
            this.rotuloConfigResumo.setText("Épocas executadas: " + epocasExecutadas + " | Erro Médio Final: " + String.format("%.6f", erroFinal));

            this.exibirAlerta("Treinamento Concluído", motivoFinalizacao + "\n" +
                    "Épocas: " + epocasExecutadas + "\nErro Médio: " + String.format("%.6f", erroFinal) + "\n\n" +
                    "Você já pode avaliar a rede na Aba 3 (Avaliação & Resultados).", Alert.AlertType.INFORMATION);
        });
    }

    /**
     * Aplica a rede treinada à base de teste e gera a Matriz de Confusão.
     */
    private void executarAvaliacaoTeste() {
        if (this.redeNeural == null || this.conjuntoTeste.getQuantidadeExemplos() == 0) {
            this.exibirAlerta("Aviso", "Treine a rede neural antes de avaliar na base de teste.", Alert.AlertType.WARNING);
        } else {
            ArrayList<String> nomesClasses = this.conjuntoTreino.getClassesUnicas();
            this.matrizConfusaoAtual = new MatrizConfusao(nomesClasses);

            ArrayList<ExemploDado> exemplosTeste = this.conjuntoTeste.getExemplos();
            int totalTeste = exemplosTeste.size();

            for (int i = 0; i < totalTeste; i = i + 1) {
                ExemploDado exemplo = exemplosTeste.get(i);
                double[] entradas = exemplo.getAtributosNormalizados();

                // Propaga as entradas pelo modelo treinado
                this.redeNeural.propagarEntradas(entradas);

                // Obtém o índice da classe prevista (neurônio com maior saída)
                int indicePrevisto = this.redeNeural.obterIndiceClassePrevista();

                // Descobre o índice da classe real do exemplo
                String rotuloReal = exemplo.getRotuloClasse();
                int indiceReal = -1;
                boolean achou = false;
                int k = 0;
                while (k < nomesClasses.size() && !achou) {
                    if (nomesClasses.get(k).equals(rotuloReal)) {
                        indiceReal = k;
                        achou = true;
                    }
                    k = k + 1;
                }

                if (indiceReal != -1) {
                    this.matrizConfusaoAtual.registrarPrevisao(indiceReal, indicePrevisto);
                }
            }

            // Atualiza indicadores visuais
            double acuracia = this.matrizConfusaoAtual.getAcuraciaGeral();
            int acertos = this.matrizConfusaoAtual.getTotalAcertos();
            this.kpiAcuracia.setText(String.format("%.2f%%", acuracia));
            this.rotuloResumoAcertos.setText(acertos + " de " + totalTeste + " exemplos classificados corretamente");

            // Desenha a Matriz de Confusão
            this.painelMatrizConfusao.desenharMatriz(this.matrizConfusaoAtual);

            // Preenche a tabela de Precisão e Revocação por classe
            this.tabelaMetricasClasses.getItems().clear();
            for (int c = 0; c < nomesClasses.size(); c = c + 1) {
                String nome = nomesClasses.get(c);
                int totalExemplos = this.matrizConfusaoAtual.getTotalClasseReal(c);
                int acertosClasse = this.matrizConfusaoAtual.getValor(c, c);
                double precisao = this.matrizConfusaoAtual.getPrecisaoClasse(c);
                double revocacao = this.matrizConfusaoAtual.getRevocacaoClasse(c);

                MetricaClasse metrica = new MetricaClasse(
                        nome,
                        String.valueOf(totalExemplos),
                        String.valueOf(acertosClasse),
                        String.format("%.2f%%", precisao),
                        String.format("%.2f%%", revocacao)
                );
                this.tabelaMetricasClasses.getItems().add(metrica);
            }

            this.botaoExportarMatriz.setDisable(false);
            this.painelAbas.getSelectionModel().select(this.abaAvaliacao);
        }
    }

    private void exportarHistoricoParaCsv() {
        if (this.treinador == null || this.treinador.getHistoricoTreinamento().size() == 0) {
            this.exibirAlerta("Aviso", "Nenhum histórico de treinamento disponível para exportar.", Alert.AlertType.WARNING);
        } else {
            FileChooser seletor = new FileChooser();
            seletor.setTitle("Exportar Histórico de Erro (CSV)");
            seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv"));
            seletor.setInitialFileName("historico_treinamento.csv");
            File arquivo = seletor.showSaveDialog(this.palcoPrincipal);
            if (arquivo != null) {
                boolean ok = ExportadorDados.exportarHistoricoErroCsv(arquivo, this.treinador.getHistoricoTreinamento());
                if (ok) {
                    this.exibirAlerta("Sucesso", "Histórico de erros exportado com sucesso!", Alert.AlertType.INFORMATION);
                } else {
                    this.exibirAlerta("Erro", "Falha ao exportar arquivo.", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void exportarMatrizParaCsv() {
        if (this.matrizConfusaoAtual == null) {
            this.exibirAlerta("Aviso", "Execute a avaliação no teste antes de exportar a matriz.", Alert.AlertType.WARNING);
        } else {
            FileChooser seletor = new FileChooser();
            seletor.setTitle("Exportar Matriz de Confusão (CSV)");
            seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv"));
            seletor.setInitialFileName("matriz_confusao.csv");
            File arquivo = seletor.showSaveDialog(this.palcoPrincipal);
            if (arquivo != null) {
                boolean ok = ExportadorDados.exportarMatrizConfusaoCsv(arquivo, this.matrizConfusaoAtual);
                if (ok) {
                    this.exibirAlerta("Sucesso", "Matriz de Confusão exportada com sucesso!", Alert.AlertType.INFORMATION);
                } else {
                    this.exibirAlerta("Erro", "Falha ao exportar arquivo.", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void adicionarLog(String texto) {
        this.areaLogEventos.appendText("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + texto + "\n");
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    /**
     * DTO simples para preenchimento da tabela de desempenho por classe.
     */
    public static class MetricaClasse {
        private final SimpleStringProperty classe;
        private final SimpleStringProperty exemplos;
        private final SimpleStringProperty acertos;
        private final SimpleStringProperty precisao;
        private final SimpleStringProperty revocacao;

        public MetricaClasse(String classe, String exemplos, String acertos, String precisao, String revocacao) {
            this.classe = new SimpleStringProperty(classe);
            this.exemplos = new SimpleStringProperty(exemplos);
            this.acertos = new SimpleStringProperty(acertos);
            this.precisao = new SimpleStringProperty(precisao);
            this.revocacao = new SimpleStringProperty(revocacao);
        }

        public SimpleStringProperty classeProperty() { return this.classe; }
        public SimpleStringProperty exemplosProperty() { return this.exemplos; }
        public SimpleStringProperty acertosProperty() { return this.acertos; }
        public SimpleStringProperty precisaoProperty() { return this.precisao; }
        public SimpleStringProperty revocacaoProperty() { return this.revocacao; }
    }
}

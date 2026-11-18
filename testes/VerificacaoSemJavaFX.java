import com.example.backpropagationfx.modelo.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

/** Auditoria repetivel do nucleo Java (sem JavaFX e sem biblioteca de IA). */
public class VerificacaoSemJavaFX {
    private static int testes = 0;

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        testes = testes + 1;
    }

    private static double obterErro(RedeNeuralMLP rede, double[] entrada, double[] desejado) {
        rede.propagarEntradas(entrada);
        double erro = rede.calcularErroExemplo(desejado);
        return erro;
    }

    private static void verificarGradientes(int tipoFuncao) {
        RedeNeuralMLP rede = new RedeNeuralMLP(2, 3, 2, tipoFuncao);
        rede.inicializarPesosAleatorios(true, 42);
        double[] entrada = {0.3, 0.7};
        double[] desejado = {1.0, 0.0};
        if (tipoFuncao == 3) {
            desejado[1] = -1.0;
        }
        double[][] w1 = rede.getPesosEntradaOculta();
        double[][] w2 = rede.getPesosOcultaSaida();
        double epsilon = 1e-6;
        double eta = 0.001;
        double[][] g1 = new double[3][2];
        double[][] g2 = new double[2][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                double antigo = w1[i][j];
                w1[i][j] = antigo + epsilon;
                double mais = obterErro(rede, entrada, desejado);
                w1[i][j] = antigo - epsilon;
                double menos = obterErro(rede, entrada, desejado);
                w1[i][j] = antigo;
                g1[i][j] = (mais - menos) / (2.0 * epsilon);
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                double antigo = w2[i][j];
                w2[i][j] = antigo + epsilon;
                double mais = obterErro(rede, entrada, desejado);
                w2[i][j] = antigo - epsilon;
                double menos = obterErro(rede, entrada, desejado);
                w2[i][j] = antigo;
                g2[i][j] = (mais - menos) / (2.0 * epsilon);
            }
        }
        double[][] antes1 = new double[3][2];
        double[][] antes2 = new double[2][3];
        for (int i=0;i<3;i++) for(int j=0;j<2;j++) antes1[i][j] = w1[i][j];
        for (int i=0;i<2;i++) for(int j=0;j<3;j++) antes2[i][j] = w2[i][j];
        rede.treinarExemplo(entrada, desejado, eta);
        for (int i=0;i<3;i++) for(int j=0;j<2;j++) {
            verificar(Math.abs((w1[i][j] - antes1[i][j]) / eta + g1[i][j]) < 1e-6,
                    "Gradiente Entrada-Oculta incorreto para funcao " + tipoFuncao);
        }
        for (int i=0;i<2;i++) for(int j=0;j<3;j++) {
            verificar(Math.abs((w2[i][j] - antes2[i][j]) / eta + g2[i][j]) < 1e-6,
                    "Gradiente Oculta-Saida incorreto para funcao " + tipoFuncao);
        }
    }

    public static void main(String[] args) throws Exception {
        for (int tipo=1;tipo<=3;tipo++) verificarGradientes(tipo);
        System.out.println("Gradientes numericos conferidos nas 3 funcoes de ativacao.");

        File arquivoInvalido = File.createTempFile("csv-invalido", ".csv");
        Files.writeString(arquivoInvalido.toPath(), "A,B,classe\n1,2,X\n3,Y\n");
        ConjuntoDados invalido = new ConjuntoDados();
        verificar(!invalido.carregarArquivoCsv(arquivoInvalido), "CSV inconsistente precisa ser recusado");
        verificar(invalido.getQuantidadeExemplos() == 0, "CSV inconsistente deve ser descartado");
        arquivoInvalido.delete();

        ConjuntoDados treino = new ConjuntoDados();
        ConjuntoDados teste = new ConjuntoDados();
        verificar(treino.carregarArquivoCsv(new File("testes/uci_iris_treino.csv")), "Leitura treino UCI");
        verificar(teste.carregarArquivoCsv(new File("testes/uci_iris_teste.csv")), "Leitura teste UCI");
        verificar(treino.getQuantidadeExemplos() == 105 && teste.getQuantidadeExemplos() == 45, "Divisao 105/45");
        verificar(treino.getQuantidadeAtributos() == 4 && treino.getQuantidadeClasses() == 3, "Arquitetura 4/3");
        treino.calcularMinimosEMaximos();
        treino.normalizar(treino.getMenoresValores(), treino.getMaioresValores());
        teste.normalizar(treino.getMenoresValores(), treino.getMaioresValores());
        verificar(teste.getMenoresValores() == treino.getMenoresValores(), "Minimos do treino reutilizados");
        verificar(teste.getMaioresValores() == treino.getMaioresValores(), "Maximos do treino reutilizados");
        treino.codificarSaidasOneHot(2, treino.getClassesUnicas());
        teste.codificarSaidasOneHot(2, treino.getClassesUnicas());

        ConjuntoDados completo = new ConjuntoDados();
        verificar(completo.carregarArquivoCsv(new File("testes/uci_iris_completo.csv")), "Arquivo unico UCI");
        ConjuntoDados[] divisao = completo.dividirTreinoETeste(70, true, 42);
        verificar(divisao[0].getQuantidadeExemplos() == 105 && divisao[1].getQuantidadeExemplos() == 45, "Divisao percentual e embaralhada");

        RedeNeuralMLP rede = new RedeNeuralMLP(4, 4, 3, 2);
        rede.inicializarPesosAleatorios(true, 42);
        final int[] chamadas = {0};
        OuvinteTreinamento ouvinte = new OuvinteTreinamento() {
            public void aoAtualizarProgresso(int epoca, double erro, double eta, long tempo) { chamadas[0]++; }
            public void aoDetectarPlato(double desvio, double eta) { throw new AssertionError("Plato inesperado"); }
            public void aoFinalizarTreinamento(String motivo, int epocas, double erro) { }
        };
        TreinadorMLP treinador = new TreinadorMLP(rede, treino, 0.2, true, 5, true, 1e-12, ouvinte);
        treinador.run();
        verificar(treinador.getTotalEpocasExecutadas() == 5, "Parada por epocas");
        verificar(treinador.getMotivoEncerramento().contains("máximo de épocas"), "Motivo por epocas");
        verificar(chamadas[0] == 5, "Callback por epoca");
        rede.inicializarPesosAleatorios(true, 42);
        treinador = new TreinadorMLP(rede, treino, 0.2, true, 30, true, 10, ouvinte);
        treinador.run();
        verificar(treinador.getTotalEpocasExecutadas() == 1, "Parada por limiar de erro");
        verificar(treinador.getMotivoEncerramento().contains("Limiar de erro"), "Motivo por erro");

        // Plateau: 10 epocas de erro estavel devem oferecer decisao e reduzir eta.
        RedeNeuralMLP redePlato = new RedeNeuralMLP(4, 4, 3, 2);
        redePlato.inicializarPesosAleatorios(true, 42);
        final TreinadorMLP[] refPlato = {null};
        final int[] platOs = {0};
        OuvinteTreinamento ouvintePlato = new OuvinteTreinamento() {
            public void aoAtualizarProgresso(int epoca, double erro, double eta, long tempo) { }
            public void aoDetectarPlato(double desvio, double eta) {
                platOs[0]++;
                refPlato[0].aplicarDecisaoPlato(3);
            }
            public void aoFinalizarTreinamento(String motivo, int epocas, double erro) { }
        };
        refPlato[0] = new TreinadorMLP(redePlato, treino, 1e-16, true, 12, false, 0, ouvintePlato);
        refPlato[0].run();
        verificar(platOs[0] == 1, "Deteccao de plato apos janela de 10 epocas");
        verificar(Math.abs(refPlato[0].getTaxaAprendizagem() - 9e-17) < 1e-22, "Reducao do eta em 10 por cento");
        verificar(refPlato[0].getTotalEpocasExecutadas() == 12, "Retomada e parada final por epocas");

        ArrayList<String> nomes = new ArrayList<String>();
        nomes.add("A"); nomes.add("B");
        MatrizConfusao m = new MatrizConfusao(nomes);
        m.registrarPrevisao(0,0);m.registrarPrevisao(0,1);m.registrarPrevisao(1,1);
        verificar(Math.abs(m.getAcuraciaGeral()-66.6666666667)<1e-6,"Acuracia global");
        verificar(Math.abs(m.getAcuraciaClasse(0)-50)<1e-6,"Acuracia classe A");
        verificar(Math.abs(m.getAcuraciaClasse(1)-100)<1e-6,"Acuracia classe B");
        verificar(Math.abs(m.getPrecisaoClasse(1)-50)<1e-6,"Precisao por classe");
        System.out.println("Validacoes de CSV, split, normalizacao, criterios e matriz: OK.");
        System.out.println("ASSERTIVAS BEM-SUCEDIDAS: " + testes);
    }
}

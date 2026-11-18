package com.example.backpropagationfx.modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

// representa um conjunto de dados carregado a partir de um arquivo CSV
// gerencia: a lista de exemplos, nomes de colunas, classes únicas, valores mínimos e máximos para normalização e divisão de treino/teste.
public class ConjuntoDados {

    // nomes das colunas de entrada obtidos do cabeçalho
    private ArrayList<String> nomesAtributos;

    // lista de todas as classes distintas encontradas na base
    private ArrayList<String> classesUnicas;

    // lista contendo todos os exemplos carregados
    private ArrayList<ExemploDado> exemplos;

    // menores valores de cada atributo calculados na base de treino
    private double[] menoresValores;

    // maiores valores de cada atributo calculados na base de treino
    private double[] maioresValores;

    public ConjuntoDados() {
        this.nomesAtributos = new ArrayList<String>();
        this.classesUnicas = new ArrayList<String>();
        this.exemplos = new ArrayList<ExemploDado>();
    }

    public ArrayList<String> getNomesAtributos() {
        return this.nomesAtributos;
    }

    public ArrayList<String> getClassesUnicas() {
        return this.classesUnicas;
    }

    public ArrayList<ExemploDado> getExemplos() {
        return this.exemplos;
    }

    public double[] getMenoresValores() {
        return this.menoresValores;
    }

    public void setMenoresValores(double[] menores) {
        this.menoresValores = menores;
    }

    public double[] getMaioresValores() {
        return this.maioresValores;
    }

    public void setMaioresValores(double[] maiores) {
        this.maioresValores = maiores;
    }

    public int getQuantidadeAtributos() {
        int total = 0;
        if (this.nomesAtributos != null) {
            total = this.nomesAtributos.size();
        }
        return total;
    }

    public int getQuantidadeClasses() {
        int total = 0;
        if (this.classesUnicas != null) {
            total = this.classesUnicas.size();
        }
        return total;
    }

    public int getQuantidadeExemplos() {
        int total = 0;
        if (this.exemplos != null) {
            total = this.exemplos.size();
        }
        return total;
    }

    // carrega os dados de um arquivo CSV
    // a última coluna é sempre considerada como a classe e as outras demais colunas são os atributos de entrada.
    public boolean carregarArquivoCsv(File arquivo) {
        boolean sucesso = true;
        this.nomesAtributos.clear();
        this.classesUnicas.clear();
        this.exemplos.clear();

        BufferedReader leitor = null;
        try {
            leitor = new BufferedReader(new FileReader(arquivo));
            String linha = leitor.readLine();

            if (linha != null) {
                String[] cabecalho = linha.split(",");
                int qtdColunas = cabecalho.length;
                if (qtdColunas < 2) {
                    throw new IOException("CSV deve conter atributos e classe.");
                }

                // guarda os nomes dos atributos (todas as colunas tirando a última)
                for (int i = 0; i < qtdColunas - 1; i = i + 1) {
                    String nome = cabecalho[i].trim();
                    this.nomesAtributos.add(nome);
                }

                // lê as linhas com os dados
                linha = leitor.readLine();
                while (linha != null) {
                    linha = linha.trim();
                    if (linha.length() > 0) {
                        String[] partes = linha.split(",", -1);
                        if (partes.length != qtdColunas) {
                            throw new IOException("Linha do CSV com quantidade incorreta de colunas.");
                        } else {
                            double[] valoresEntrada = new double[qtdColunas - 1];
                            for (int i = 0; i < qtdColunas - 1; i = i + 1) {
                                valoresEntrada[i] = Double.parseDouble(partes[i].trim());
                                if (!Double.isFinite(valoresEntrada[i])) {
                                    throw new IOException("Atributo numerico invalido no CSV.");
                                }
                            }

                            // rótulo da classe usando trim para remover espaços em branco
                            String rotulo = partes[qtdColunas - 1].trim();
                            if (rotulo.isEmpty()) {
                                throw new IOException("Classe vazia no CSV.");
                            }

                            // verifica se a classe já está na lista de classes
                            boolean classeExiste = false;
                            int k = 0;
                            while (k < this.classesUnicas.size() && !classeExiste) {
                                if (this.classesUnicas.get(k).equals(rotulo)) {
                                    classeExiste = true;
                                }
                                k = k + 1;
                            }

                            if (!classeExiste) {
                                this.classesUnicas.add(rotulo);
                            }

                            ExemploDado novoExemplo = new ExemploDado(valoresEntrada, rotulo);
                            this.exemplos.add(novoExemplo);
                        }
                    }
                    linha = leitor.readLine();
                }
            } else {
                sucesso = false;
            }
            if (this.exemplos.isEmpty()) {
                sucesso = false;
            }

            leitor.close();
        } catch (Exception e) {
            sucesso = false;
            if (leitor != null) {
                try {
                    leitor.close();
                } catch (IOException ex) {
                    // Ignora falha secundária no fechamento
                }
            }
        }
        if (!sucesso) {
            this.nomesAtributos.clear();
            this.classesUnicas.clear();
            this.exemplos.clear();
        }

        return sucesso;
    }

    // calcula os valores mínimo e máximo de cada atributo na base de treinamento.
    public void calcularMinimosEMaximos() {
        int qtdAtributos = this.getQuantidadeAtributos();
        int qtdExemplos = this.getQuantidadeExemplos();

        if (qtdAtributos > 0 && qtdExemplos > 0) {
            this.menoresValores = new double[qtdAtributos];
            this.maioresValores = new double[qtdAtributos];

            for (int col = 0; col < qtdAtributos; col = col + 1) {
                double primeiroValor = this.exemplos.get(0).getAtributosOriginais()[col];
                double menor = primeiroValor;
                double maior = primeiroValor;

                for (int lin = 1; lin < qtdExemplos; lin = lin + 1) {
                    double valor = this.exemplos.get(lin).getAtributosOriginais()[col];
                    if (valor < menor) {
                        menor = valor;
                    }
                    if (valor > maior) {
                        maior = valor;
                    }
                }

                this.menoresValores[col] = menor;
                this.maioresValores[col] = maior;
            }
        }
    }

    // normaliza os atributos de entrada de cada exemplo para a faixa [0, 1]
    // utiliza os menores e maiores valores informados (provenientes do treino)
    // e trata o caso especial em que o máximo é igual ao mínimo.
    public void normalizar(double[] menores, double[] maiores) {
        this.menoresValores = menores;
        this.maioresValores = maiores;

        int qtdAtributos = this.getQuantidadeAtributos();
        int qtdExemplos = this.getQuantidadeExemplos();

        for (int lin = 0; lin < qtdExemplos; lin = lin + 1) {
            ExemploDado exemplo = this.exemplos.get(lin);
            double[] originais = exemplo.getAtributosOriginais();
            double[] normalizados = new double[qtdAtributos];

            for (int col = 0; col < qtdAtributos; col = col + 1) {
                double min = menores[col];
                double max = maiores[col];
                double intervalo = max - min;

                if (intervalo == 0.0) {
                    // Evita divisão por zero se o valor for constante em toda a base
                    normalizados[col] = 0.0;
                } else {
                    double valorCalculado = (originais[col] - min) / intervalo;
                    normalizados[col] = valorCalculado;
                }
            }

            exemplo.setAtributosNormalizados(normalizados);
        }
    }

    // monta o vetor de saída desejada para cada exemplo
    // para Linear e Logística: 1.0 na classe correspondente e 0.0 nas demais
    // para Tangente Hiperbólica: 1.0 na classe correspondente e -1.0 nas outras
    public void codificarSaidasOneHot(int tipoFuncao, ArrayList<String> listaClassesReferencia) {
        int qtdClasses = listaClassesReferencia.size();
        int qtdExemplos = this.getQuantidadeExemplos();

        double valorInativo = 0.0;
        if (tipoFuncao == 3) {
            // Tangente Hiperbólica tem saída entre -1 e 1
            valorInativo = -1.0;
        }

        for (int lin = 0; lin < qtdExemplos; lin = lin + 1) {
            ExemploDado exemplo = this.exemplos.get(lin);
            double[] vetorDesejado = new double[qtdClasses];

            for (int c = 0; c < qtdClasses; c = c + 1) {
                String nomeClasseRef = listaClassesReferencia.get(c);
                if (exemplo.getRotuloClasse().equals(nomeClasseRef)) {
                    vetorDesejado[c] = 1.0;
                } else {
                    vetorDesejado[c] = valorInativo;
                }
            }

            exemplo.setVetorDesejado(vetorDesejado);
        }
    }

    // divide este conjunto de dados em dois novos conjuntos (treino e teste)
    // e de acordo com a porcentagem indicada (tipo 70.0 para 70% de treino)
    // aplica embaralhamento usando uma semente (seed) aleatória ou "aleatório puro" se usarSeed for falso
    public ConjuntoDados[] dividirTreinoETeste(double porcentagemTreino, boolean usarSeed, long valorSeed) {
        ConjuntoDados[] resultado = new ConjuntoDados[2];
        ConjuntoDados baseTreino = new ConjuntoDados();
        ConjuntoDados baseTeste = new ConjuntoDados();

        // copia referências de atributos e classes
        for (int i = 0; i < this.nomesAtributos.size(); i = i + 1) {
            baseTreino.getNomesAtributos().add(this.nomesAtributos.get(i));
            baseTeste.getNomesAtributos().add(this.nomesAtributos.get(i));
        }
        for (int i = 0; i < this.classesUnicas.size(); i = i + 1) {
            baseTreino.getClassesUnicas().add(this.classesUnicas.get(i));
            baseTeste.getClassesUnicas().add(this.classesUnicas.get(i));
        }

        // clona a lista para embaralhar sem alterar a ordem visual original
        ArrayList<ExemploDado> listaCopia = new ArrayList<ExemploDado>();
        for (int i = 0; i < this.exemplos.size(); i = i + 1) {
            listaCopia.add(this.exemplos.get(i));
        }

        // embaralhamento
        Random gerador = null;
        if (usarSeed) {
            gerador = new Random(valorSeed);
        } else {
            gerador = new Random();
        }

        for (int i = listaCopia.size() - 1; i > 0; i = i - 1) {
            int indiceSorteado = gerador.nextInt(i + 1);
            ExemploDado temporario = listaCopia.get(i);
            listaCopia.set(i, listaCopia.get(indiceSorteado));
            listaCopia.set(indiceSorteado, temporario);
        }

        int totalExemplos = listaCopia.size();
        int qtdTreino = (int) Math.round((totalExemplos * porcentagemTreino) / 100.0);

        for (int i = 0; i < totalExemplos; i = i + 1) {
            if (i < qtdTreino) {
                baseTreino.getExemplos().add(listaCopia.get(i));
            } else {
                baseTeste.getExemplos().add(listaCopia.get(i));
            }
        }

        resultado[0] = baseTreino;
        resultado[1] = baseTeste;
        return resultado;
    }
}


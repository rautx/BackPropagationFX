BACKPROPAGATIONFX - TRABALHO DE IA (2o BIMESTRE)
================================================

GRUPO (preencher antes de entregar):
- Integrante 1: [NOME COMPLETO]
- Integrante 2: [NOME COMPLETO]
- Integrante 3: [NOME COMPLETO, se aplicavel]
Turma: [TURMA]
Disciplina: Inteligencia Artificial

1. PROPOSTA DE SOLUCAO
Classificar a especie de uma flor Iris (setosa, versicolor ou virginica)
a partir de quatro medidas numericas de sepalas e petalas, por meio de
rede neural MLP com Backpropagation implementado em Java, sem biblioteca de IA.

Fonte dos dados: UCI Machine Learning Repository - Iris, id 53.
https://archive.ics.uci.edu/dataset/53/iris
DOI: https://doi.org/10.24432/C56C76
150 amostras, 4 atributos, 3 classes.
Arquivos incluidos em testes/:
- uci_iris_completo.csv (para o modo arquivo unico, com divisao percentual)
- uci_iris_treino.csv (105 registros)
- uci_iris_teste.csv (45 registros)
A divisao de exemplo foi estratificada, com 35 por classe no treino e 15 por
classe no teste, embaralhadas com semente 42.
Os arquivos base_treinamento.csv e base_teste.csv ja estavam no projeto
original; sua origem publica nao foi informada. Sao exemplos adicionais.

2. LINGUAGEM, FERRAMENTAS E BIBLIOTECAS
- Java 21 (JDK 21 ou superior compativel com configuracao Maven)
- Maven Wrapper incluido (.mvn/, mvnw, mvnw.cmd); ele baixa Maven quando preciso
- JavaFX Controls / FXML 21.0.6 para interface, graficos e tabela
- JUnit Jupiter 5.12.1 apenas para testes automatizados
- Sem TensorFlow, Keras, PyTorch, scikit-learn, Weka ou biblioteca de IA.
A leitura do CSV, normalizacao, MLP, Backpropagation, avaliacao e matriz
sao implementadas manualmente no codigo-fonte do projeto.

3. COMO EXECUTAR NO WINDOWS
Instale um JDK 21 e configure JAVA_HOME. Tenha internet no PRIMEIRO uso
para que o Maven Wrapper baixe Maven e as bibliotecas de interface/teste.
Abra um terminal na pasta que contem pom.xml e execute:

  .\mvnw.cmd clean test
  .\mvnw.cmd javafx:run

Linux/macOS (com ambiente grafico):
  ./mvnw clean test
  ./mvnw javafx:run

Alternativamente, abra o pom.xml no IntelliJ IDEA como projeto Maven,
aguarde resolver dependencias e execute MainApplication ou javafx:run.

4. ROTEIRO DE USO EM LABORATORIO
Aba 1 - Base de Dados:
- Escolha "dois arquivos" e carregue uci_iris_treino.csv e
  uci_iris_teste.csv. Clique em "Carregar e Processar Dados".
- Alternativamente selecione "arquivo unico" e uci_iris_completo.csv,
  70% de treino e seed de divisao opcional. Essa segunda divisao sera
  novamente aleatoria, nao necessariamente identica aos CSV separados.
Aba 2 - Configuracao e Treinamento:
- A rede detecta automaticamente 4 entradas e 3 classes de saida.
- Escolha a camada oculta (media aritmetica, geometrica ou manual).
- Escolha linear, logistica ou tangente hiperbolica.
- Informe taxa 0 < eta <= 1, epocas e/ou erro minimo; seed opcional.
- Inicie. Veja grafico, indicadores, pesos por hover e alerta de plato.
- Pode pausar, continuar, reduzir eta 10% no plato ou interromper.
Aba 3 - Avaliacao e Resultados:
- Clique para testar; observe matriz, acuracia global, acuracia por classe,
  precisao e revocacao. Exporte CSV se desejar.

5. DECISOES DE IMPLEMENTACAO
- Peso inicial uniforme aleatorio no intervalo [-0,5, +0,5].
- Uma camada oculta. Neuronios ocultos: media aritmetica de E e S,
  geometrica raiz(E x S), com Math.round, ou numero manual positivo.
- Sem bias (nenhuma conexao de termo constante).
- Funcoes: linear = net/10; logistica; tanh, com derivadas analiticas.
- Codificacao desejada: 1 na classe correta, 0 nas outras (linear/logistica);
  +1/-1 para tangente hiperbolica, de acordo com a faixa da funcao.
- Atualizacao dos pesos apos cada exemplo (online), no sentido oposto
  ao gradiente do erro quadratico.
- Erro por exemplo: metade da soma dos erros quadraticos das saidas;
  por epoca: media dos erros dos exemplos de treino.
- Normalizacao min-max: min/max apenas do treino e reutilizados no teste.
  Amostras de teste fora do intervalo do treino podem gerar valores fora
  de [0,1], sem recorte artificial para evitar alterar o dado.
- Teste nao participa das atualizacoes dos pesos.
- Classificacao: indice do maior neuronio da camada de saida.
- Acuracia global: total de acertos / total de testes.
- Acuracia por classe: acertos da classe / total real da classe;
  esta metrica e igual a revocacao por classe neste problema multiclasse.
- O detector de plato usa desvio padrao de 10 epocas e limiar 0,00001.

6. EXPERIMENTOS, RELATORIO E EXPORTACOES
- RELATORIO_EXPERIMENTOS.pdf: 3 experimentos, curvas de erro, matrizes
  de confusao e analise (2 paginas).
- testes/experimentos: arquivos CSV de curvas e matrizes, alem de imagens
  das curvas, gerados por execucoes do nucleo Java do projeto.
- Experimento 1: logistica, eta=0,2, 4 ocultos, 250 epocas -> 43/45 = 95,56%.
- Experimento 2: logistica, eta=0,4, 6 ocultos, 250 epocas -> 43/45 = 95,56%.
- Experimento 3: tanh, eta=0,2, 6 ocultos, 250 epocas -> 39/45 = 86,67%.
Os experimentos foram feitos diretamente com as classes do modelo por
250 epocas, sem intervencao do dialogo de plato da interface.

7. ATENCAO ANTES DA ENTREGA
- Preencha os nomes dos integrantes e a turma neste arquivo e no PDF.
- Execute a interface JavaFX no computador do laboratorio para validar
  a demonstracao visual e o funcionamento dos controles graficos.
- Todos os integrantes devem conhecer a implementacao e os resultados.

8. VERIFICACAO DO NUCLEO SEM JAVAFX (OPCIONAL)
No Windows, execute verificar_nucleo.cmd com JDK 21 instalado. O programa
compila apenas a pasta modelo/ e executa testes de gradientes, leitura,
split, normalizacao, parada, plato e matriz, sem dependencias externas.
Resultado observado nesta auditoria: 58 assertivas bem-sucedidas.

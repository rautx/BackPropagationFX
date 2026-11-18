# BackPropagationFX - Rede Neural Perceptron Multicamadas (MLP)

Trabalho pratico da disciplina de Inteligencia Artificial.

Alunos: Raul Santos Teixeira e Yuri Lanuti
Turma: A, 6° Termo, Inteligencia Artificial I

---

## 1. Descricao Geral

Este projeto e uma implementacao em Java de uma Rede Neural Artificial MLP utilizando o algoritmo Backpropagation para treinamento supervisionado. A aplicacao conta com uma interface grafica feita em JavaFX que permite carregar bases de dados em CSV, configurar os parametros da rede, acompanhar o treinamento em tempo real com graficos e avaliar os resultados atraves de uma matriz de confusao com metricas detalhadas por classe.

Todo o algoritmo da rede neural foi feito de forma manual, sem utilizar bibliotecas prontas de inteligencia artificial.

---

## 2. Linguagem e Versao

- Linguagem: Java
- Versao: Java 21 (LTS)
- Gerenciador de dependencias: Apache Maven (com Maven Wrapper incluso no projeto)

---

## 3. Como Executar o Programa

### Execucao pelo IntelliJ IDEA (Recomendado)

1. Abra o IntelliJ IDEA.
2. Clique em Open e selecione a pasta do projeto (ou o arquivo `pom.xml`).
3. Escolha a opcao Open as Project e aguarde o IntelliJ baixar as dependencias do JavaFX.
4. Navegue ate a pasta `src/main/java/com/example/backpropagationfx/`.
5. Clique com o botao direito no arquivo `Launcher.java` e selecione `Run 'Launcher.main()'`.

Se deve usar o Launcher.java, pois o JavaFX mais recente exige modulos especificos ao iniciar pela classe principal direta. O arquivo Launcher apenas repassa a inicializacao para a aplicacao visual sem disparar erros de inicializacao no Windows. Ou seja, isso é algo do JavaFX.
E mais uma coisa, o arquivo pode ocasionar erros, caso ocorra, exclua a pasta `.idea`, e abra novamente com o Intelij, ele vai recriar a pasta de forma correta.

### Execucao pelo Terminal / Linha de Comando

Se preferir rodar direto pelo terminal sem abrir o IntelliJ:

No Windows (Prompt de Comando ou PowerShell):
```bash
.\mvnw.cmd clean compile
.\mvnw.cmd javafx:run
```

No Linux ou macOS:
```bash
./mvnw clean compile
./mvnw javafx:run
```

---

## 4. Bibliotecas Utilizadas

O arquivo `pom.xml` possui apenas as bibliotecas essenciais para a interface visual e para os testes do Java:

javafx-controls e javafx-fxml (versao 21.0.6)
   - O que faz: Responsavel pela janela, botoes, abas, tabelas e pelos componentes graficos (o grafico de linha da curva de erro `LineChart` e o grafico de setores `PieChart`).
   - Nao e biblioteca de IA: Trata-se de uma biblioteca padrao de interface grafica da Oracle/OpenJFX. Ela apenas desenha elementos na tela e não possui nenhuma funcao de aprendizado, redes neurais ou algebra linear.

Comprovacao: Não foram usadas bibliotecas como TensorFlow, PyTorch, Weka, Scikit-Learn ou Deeplearning4j. A propagacao direta (forward), o calculo dos gradientes, a atualizacao dos pesos, a normalizacao e a matriz de confusao foram todos desenvolvidos do zero nas classes do pacote modelo.

---

## 5. Decisoes de Implementacao

Seguindo a mesma modelagem de desenvolvimento dos nossos trabalhos anteriores, separamos o projeto entre a camada logica (`modelo`) e a camada visual (`visao`). As principais escolhas tecnicas foram:

### 5.1 Inicializacao dos Pesos Sinapticos
- Arquivo: `RedeNeuralMLP.java` (metodo `inicializarPesosAleatorios`)
- Os pesos de todas as conexoes da rede sao sorteados de forma aleatoria no intervalo de -0.5 a +0.5.
- O sistema permite marcar o uso de semente (seed). Com a seed ativada, o gerador de numeros aleatorios repete exatamente a mesma sequencia de pesos, permitindo comparar testes com a mesma condicao inicial.
- A cada clique no botao de iniciar treinamento, a rede reseta seus pesos para evitar que um treino anterior interfira no novo.

### 5.2 Nao Utilizacao de Bias
- A rede não possui uso de bias.
- Cada neuronio intermediario e de saida calcula seu `net` pela multiplicacao das entradas pelos pesos que chegam ate ele. Posteriormente aplicamos o uso de Seeds.

### 5.3 Calculo dos Neuronios da Camada Oculta
- Arquivo: `TelaPrincipal.java` (metodo `atualizarCalculoOculta`)
- A rede trabalha com uma unica camada oculta, oferecendo tres opcoes de escolha:
  1. Media Aritmetica: `(Entradas + Saidas) / 2`, com resultado arredondado via `Math.round`.
  2. Media Geometrica: raiz quadrada de `(Entradas * Saidas)`, tambem arredondada via `Math.round`.
  3. Manual: o usuario pode digitar livremente qualquer quantidade inteira de neuronios.

### 5.4 Funcoes de Transferencia (Ativacao) e Derivadas
- Arquivo: `Neuronio.java` (metodo `aplicarAtivacao`)
- 1. Linear:
  - Formula: `f(net) = net / 10`
  - Derivada: `f'(net) = 0.1`
- 2. Logistica (Sigmoide):
  - Formula: `f(net) = 1 / (1 + exp(-net))`
  - Derivada: `f'(net) = saida * (1 - saida)`
- 3. Tangente Hiperbolica:
  - Formula: calculada usando `Math.tanh(net)`. Essa funcao é matematicamente identica a formula `(1 - exp(-2*net)) / (1 + exp(-2*net))`, mas evita estouro de ponto flutuante (overflow) quando o net assume valores muito altos.
  - Derivada: `f'(net) = 1 - (saida * saida)`

### 5.5 Vetor de Saida Desejada (One-Hot Encoding)
- Arquivo: `ConjuntoDados.java` (metodo `codificarSaidasOneHot`)
- A camada de saída possui um neuronio para cada classe diferente identificada na base de dados.
- Para as funcoes Linear e Logistica, o neuronio da classe verdadeira recebe `1.0` e os demais recebem `0.0`.
- Para a Tangente Hiperbolica, o neuronio da classe verdadeira recebe `+1.0` e os outros recebem `-1.0`, acompanhando o intervalo natural da propria função.

### 5.6 Aprendizado Padrao a Padrao (Online)
- Arquivo: `TreinadorMLP.java` e `RedeNeuralMLP.java` (metodo `treinarExemplo`)
- A rede atualiza os pesos apos cada exemplo apresentado.
- Em cada padrao, o sistema faz o forward, calcula o gradiente de erro na saída, retropropaga esse erro para a camada oculta e recalcula os pesos pela regra delta: `novo_peso = peso_atual + taxa * erro_gradiente * entrada`.

### 5.7 Calculo do Erro
- Erro do Exemplo: calculado pela metade da soma dos quadrados dos erros de cada neuronio de saída: `0.5 * soma((desejado - obtido)^2)`.
- Erro Médio da Epoca: ao terminar de passar por todos os exemplos do treino, calcula-se a media aritmetica desses erros. Esse e o valor plotado na curva de erro.

### 5.8 Normalizacao dos Dados (Min-Max)
- Arquivo: `ConjuntoDados.java` (metodo `normalizar`)
- As entradas sao normalizadas para o intervalo de 0 a 1 usando a formula Min-Max.
- Cuidado tomado: os valores minimos e maximos sao calculados exclusivamente na base de treinamento e reaproveitados para normalizar a base de teste. Isso garante que o conjunto de teste nao interfira nas estatisticas do modelo.
- Caso uma coluna tenha valor constante (minimo igual ao maximo), o valor normalizado e fixado em 0.0 para nao causar divisao por zero.

### 5.9 Deteccao e Tratamento de Plato
- Arquivo: `TreinadorMLP.java` e `CalculadoraEstatistica.java`
- O treinador guarda uma lista com o erro medio das ultimas 10 epocas.
- Se o desvio padrao desses 10 valores for menor ou igual a `0.00001`, o sistema identifica que o aprendizado estagnou.
- O treino e pausado e abre-se uma janela com 3 opcoes:
  1. Interromper o treinamento.
  2. Continuar sem mudar a taxa de aprendizagem.
  3. Reduzir a taxa em 10% (`taxa = taxa * 0.9`) e continuar.
- Protecao implementada: se o usuario fechar essa janela no "X" sem escolher nenhuma opcao, o sistema interrompe o treino com seguranca para nao deixar a thread pausada infinitamente.

### 5.10 Criterios de Parada e Interrupcao
- O treino para quando atinge o limite de epocas ou quando o erro medio cai abaixo do limiar minimo configurado.
- Caso o usuario clique em Interromper no meio de uma epoca, o laco interno checa o sinal de parada e nao registra dados parciais quebrados.

---

## 6. Recursos Visuais e Interface Grafica

A interface grafica foi organizada em tres abas de navegacao para tornar o fluxo de uso intuitivo:

1. Aba 1 - Base de Dados:
   - Permite escolher entre carregar dois arquivos CSV (um para treino e outro para teste) ou carregar um arquivo unico com divisao percentual automatica.
   - Valida os arquivos na leitura: se faltar coluna, se houver campos vazios ou numeros invalidos, exibe um alerta vermelho e nao carrega dados pela metade.
   - Mostra uma tabela de previa com os dados normalizados e codificados.

2. Aba 2 - Configuracao e Treinamento:
   - Controles visuais para definir a camada oculta, funcao de ativacao, taxa de aprendizagem, epocas e limiar de erro.
   - Bloqueio de controles durante o treino: enquanto a rede esta rodando, botoes de alterar arquitetura, trocar funcao ou resetar ficam desativados para o usuario nao corromper a execucao da thread.
   - Curva de Erro (`LineChart`): desenha o erro epoca por epoca sem pular pontos, permitindo ver nitidamente a queda do erro ou a formacao de platos.
   - Visualizacao da Rede Neural (`PainelVisualizacaoRede.java`): desenha os neuronios em tres colunas e as linhas sinapticas conectando-os. As linhas verdes representam pesos positivos e as vermelhas representam pesos negativos, com a grossura da linha proporcional ao tamanho do peso. Ao posicionar o cursor do mouse sobre a linha, um texto informa o valor exato daquele peso sinaptico.

3. Aba 3 - Avaliacao e Resultados:
   - Botao para testar a rede sobre a base de teste (que nao foi usada no treino).
   - A classe prevista e escolhida pelo neuronio de saida que disparar o maior valor (argmax).
   - Matriz de Confusao (`PainelMatrizConfusao.java`): tabela visual onde as linhas sao as classes reais e as colunas sao as classes previstas. A diagonal principal e destacada em verde representando os acertos, e os erros fora da diagonal sao destacados em vermelho suave.
   - Tabela de Metricas Individuais: exibe para cada classe a quantidade de exemplos, acertos, Acuracia da Classe (taxa de acerto daquela classe), Precisao (acertos sobre o total previsto) e Revocacao.
   - Grafico de Setores / Pizza (`PieChart`): mostra de forma limpa a proporcao geral entre acertos (fatia verde) e erros (fatia vermelha) no teste.
   - Botoes de Exportacao: permitem exportar todo o historico de treinamento e a matriz de confusao para arquivos `.csv`.

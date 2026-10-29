# 🧠 MLP Backpropagation Studio — JavaFX

Projeto autoral de **Rede Neural Artificial do tipo Perceptron Multicamadas (MLP)** com algoritmo de aprendizado supervisionado **Backpropagation**, desenvolvido em **Java** com interface gráfica moderna em **JavaFX**.

---

## 🎯 Atendimento aos Requisitos

### Requisitos Funcionais Obrigatórios
- **RF01 (Carregar Dados):** Leitura de CSV com cabeçalho, detecção automática do número de atributos (entradas) e número de classes únicas (saídas), prévia dos dados em tabela e tratamento de espaços em branco (ex: `" CA"` $\to$ `"CA"`).
- **RF02 (Treino e Teste):** Suporta dois modos:
  1. Carregamento de 2 arquivos separados (treino e teste).
  2. Carregamento de 1 arquivo único com divisão percentual informada pelo usuário (ex: 70% / 30%) com embaralhamento (Fisher-Yates) e semente opcional.
- **RF03 (Normalização Min-Max):** Normalização para o intervalo $[0, 1]$. Os valores de $\min$ e $\max$ são calculados **estritamente na base de treino** e reutilizados no teste, tratando o caso $\max = \min$.
- **RF04 (Arquitetura):** Entradas = atributos, Saídas = classes. A camada oculta pode ser calculada por:
  - Média Aritmética: $(E + S) / 2$
  - Média Geométrica: $\sqrt{E \times S}$
  - Definição Manual
- **RF05 (Função de Transferência):** Linear ($net/10$), Logística Sigmoide ($[0, 1]$) e Tangente Hiperbólica ($[-1, 1]$), com suas derivadas exatas.
- **RF06 (Taxa de Aprendizagem $\eta$):** Campo de texto numérico preciso (`TextField`) aceitando valores decimais de até 6 a 8 casas no intervalo $0 < \eta \le 1$, com validação e mensagens amigáveis.
- **RF07 (Critérios de Parada):** Por número máximo de épocas e por limiar de erro mínimo. Informa qual critério encerrou o treino.
- **RF08 (Treinamento com Backpropagation):** Pesos pequenos aleatórios, atualização padrão a padrão (estocástica/online) e cálculo do erro médio da época.
- **RF09 (Acompanhamento em Tempo Real):** Indicadores de época, erro médio, $\eta$ atual, tempo decorrido, e gráfico da curva de erro (`LineChart`). Permite Pausar, Retomar e Interromper.
- **RF10 (Visualização da Arquitetura):** Desenho gráfico das camadas (Entrada, Oculta, Saída). Conexões com cor pelo sinal (azul positivo, vermelho negativo), espessura pela magnitude e **hover no mouse mostrando o peso exato $w$**.
- **RF11 (Detecção de Platô):** Monitora janela das últimas 10 épocas. Se o desvio padrão for $\le 0.00001$, pausa o treino e abre diálogo com 3 opções:
  1. Continuar reduzindo $\eta$ em 10% ($\eta_{\text{novo}} = 0.9 \cdot \eta$).
  2. Continuar sem alterar $\eta$.
  3. Interromper treinamento.
- **RF12 (Testar a Rede):** Aplica o modelo à base de teste, exibe a **Matriz de Confusão** (diagonal destacada em azul e erros em vermelho suave), acurácia geral, e tabela com **Precisão e Revocação por classe**.

### Bônus Implementados
- ✅ **Exportar Histórico do Erro e Matriz de Confusão em CSV.**
- ✅ **Fixar semente aleatória (Seed)** tanto para o embaralhamento dos dados quanto para a inicialização dos pesos.

---

## 🛠️ Regras de Código Seguidas Rigorosamente

- **Linguagem:** 100% em **Português**, com classes e métodos didáticos e fáceis de explicar em apresentações acadêmicas.
- **Estrutura Simples:** Sem operadores ternários (`? :`), sem `break`, sem `continue` e sem `return` precoce no meio de laços e condicionais.
- **Controle por Flags:** Todo o fluxo de parada, pausa, buscas e validações utiliza flags booleanas explícitas.
- **Sem Frameworks de IA:** Todo o cálculo de feedforward, backpropagation, gradientes, ativações e matriz de confusão foi desenvolvido do zero de forma autoral.

---

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   ├── module-info.java
│   │   └── com/example/backpropagationfx/
│   │       ├── Launcher.java                 # Inicializador da aplicação
│   │       ├── MainApplication.java         # Aplicação JavaFX
│   │       ├── modelo/
│   │       │   ├── Neuronio.java             # Neurônio individual (net, ativação, derivada, erro)
│   │       │   ├── RedeNeuralMLP.java        # MLP (feedforward, gradientes, backward, pesos)
│   │       │   ├── ExemploDado.java          # Linha de dados (originais, normalizados, one-hot)
│   │       │   ├── ConjuntoDados.java        # Leitor de CSV, min-max, split 70/30, shuffle
│   │       │   ├── MatrizConfusao.java       # Matriz, Acurácia, Precisão e Revocação
│   │       │   ├── RegistroEpoca.java        # DTO de histórico de treino
│   │       │   ├── CalculadoraEstatistica.java # Média e desvio padrão para platô
│   │       │   ├── TreinadorMLP.java         # Thread de treino em segundo plano
│   │       │   ├── OuvinteTreinamento.java   # Interface de callbacks para a interface
│   │       │   └── ExportadorDados.java      # Exportação para CSV (Bônus)
│   │       └── visao/
│   │           ├── TelaPrincipal.java        # Layout em abas (Dados, Treino, Avaliação)
│   │           ├── PainelVisualizacaoRede.java # Desenho da rede com hover nos pesos
│   │           └── PainelMatrizConfusao.java # Grade gráfica colorida da matriz
│   └── resources/
│       └── com/example/backpropagationfx/
│           └── estilos.css                   # Folha de estilo moderna para JavaFX
testes/
├── base_treinamento.csv                      # Base de treino com 6 atributos e 5 classes
└── base_teste.csv                            # Base de teste
```

---

## 🚀 Como Executar

No terminal dentro da pasta do projeto:

```powershell
# Compilar o projeto
.\mvnw.cmd clean compile

# Executar a aplicação
.\mvnw.cmd javafx:run
```

Ou execute a classe `Launcher.java` / `MainApplication.java` diretamente pela sua IDE (IntelliJ IDEA, Eclipse, VS Code ou NetBeans).


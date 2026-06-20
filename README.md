# Sobre o Projeto

O **ParkSys** é um sistema completo para gerenciamento de estacionamento desenvolvido com interface gráfica nativa em **Java Swing**. O projeto foi construído do zero como trabalho integrador da disciplina de **ARQDEOO**, reunindo na prática a aplicação de diversos conceitos essenciais da orientação a objetos. 

As suas funcionalidades incluem:
- Registro de entrada e saída de veículos (com validação de vagas consecutivas).
- Suporte a diversas categorias de veículos (Motos, Carros, SUVs e Caminhões), cada qual com sua ocupação de espaço e tarifação por hora diferenciadas.
- Cadastro de clientes mensalistas com reserva fixa de vagas.
- Emissão de relatórios gerenciais e financeiros diários.
- Dashboard atualizado em tempo real exibindo a lotação do estacionamento.
- Persistência e restauração orgânica do estado da aplicação entre as execuções (Serialização automática).

## Tecnologias

- **Linguagem**: Java 17+
- **Interface Gráfica**: Java Swing (com UI estilizada via custom Look & Feel)
- **Persistência**: Java Object Serialization
- **IDE Utilizada**: Visual Studio Code (VS Code) / IntelliJ IDEA

## Estrutura de Pacotes

O projeto foi separado arquiteturalmente (padrão MVC) nos seguintes pacotes:

- `parksys.main`: Ponto de entrada da aplicação, contendo a classe principal que realiza as injeções, gerencia threads de simulação e inicia a UI.
- `parksys.entities`: Classes de domínio principais (POJOs), como `Veiculo`, `Vaga`, `Registro` e `Mensalista`.
- `parksys.enums`: Enumerações de regras de negócio estritas (`TipoVeiculo` e `StatusVaga`).
- `parksys.exceptions`: Tratamentos de erro e fluxo customizados (ex: `VagaOcupadaException`).
- `parksys.services`: Concentra toda a lógica de negócio, persistência no FileSystem e processamentos concorrentes (`GerenciadorEstacionamento`, `GerenciadorArquivo`, Runnables).
- `parksys.observer`: Contratos e implementações do padrão de monitoramento reativo.
- `parksys.ui`: Toda a camada de exibição gráfica construída usando componentes `javax.swing.*`.

## Como Executar

Para executar o projeto usando puramente a linha de comando (CLI) e o JDK:

1. Abra um terminal na raiz do diretório do projeto (`parksys-junior-marques`).
2. **Compile o código** direcionando a saída para a pasta `out`:
   ```bash
   javac -sourcepath src -d out src/parksys/main/Main.java
   ```
3. **Execute a aplicação**:
   ```bash
   java -cp out parksys.main.Main
   ```
> *Nota: Ao ser executada a primeira vez, a aplicação rodará brevemente uma simulação de threads concorrentes no terminal (inserindo 4 carros simultâneos) e logo após iniciará a tela da Interface Gráfica.*

## Conceitos Aplicados

Durante o desenvolvimento do ParkSys, aplicou-se ativamente as seguintes mecânicas avançadas do Java:

- **Collections Framework**: Foi empregado o uso de estruturas diversificadas para necessidades diferentes. Exemplo: `HashMap<String, Vaga>` permitindo acesso `O(1)` às vagas cadastradas pelo ID; `TreeSet<Registro>` para exibir nativamente registros ordenados pelo tempo sem necessidade de métodos de sorting complexos; e `LinkedList<Mensalista>` para melhor eficiência em inserção/remoção frequentes nas extremidades do sistema.
- **Serialização**: Utilização das streams nativas de Input/Output do Java (`ObjectOutputStream` e `ObjectInputStream`) para o mecanismo de save automático ao fechar telas e carregamento assíncrono ao ligar o app, dispensando a necessidade de banco de dados relacional e marcando com `transient` informações em tempo de execução que não deviam ir à persistência.
- **Multithreading**: Demonstração de concorrência real simulando 4 cancelas paralelas requisitando entrada (via implements `Runnable`) e uso massivo da palavra chave `synchronized` para bloqueio de monitores instrínsecos em métodos que iteravam as coleções (evitando cenários de dupla-reserva / *Race Conditions*). Foi aplicado também uma *Thread Daemon* em `MonitorRunnable.java` que fiscaliza o estado do gerenciador repetitivamente.
- **Padrões de Projeto**: 
  - **MVC** isolando estritamente regras de negócios em `services` e inputs em `ui`.
  - **Singleton** permitindo que todas as telas Swing e background-threads partilhassem sempre da mesma instância do `GerenciadorEstacionamento`.
  - **Observer** no fluxo da UI e Monitor, desacoplando o estacionamento que dispara apenas `.notificarObservadores()`, enquanto os painéis gráficos apenas recebem o evento para redesenho automático da lotação de vagas na tela.

## Branches

A estratégia de controle de versão dividiu os requisitos propostos do professor em branches isoladas (para seguir a convenção de Conventional Commits):

- `main`: Branch de integração definitiva.
- `feature/enums`: Refatoração base das constantes de negócio (`TipoVeiculo`, `StatusVaga`).
- `feature/entities`: Serialização das entidades de domínio básicas.
- `feature/services`: Estruturação do core de regras com as Java Collections.
- `feature/serializacao`: Fluxo IO de persistência dos mapas.
- `feature/threads`: Proteção `synchronized` nas concorrências e simulações com `join()`.
- `feature/patterns`: Implementação do Singleton, Observer e MVC no núcleo de dependências.
- `feature/ui`: Construção integral do módulo Swing visual com tipografia customizada (`EstiloUI`).

## Autor(es)

- **Nome**: Davi (e companheiros se houver, preencha aqui)
- **Turma**: TSI (preencha sua identificação específica)

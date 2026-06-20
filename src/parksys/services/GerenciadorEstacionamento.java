package parksys.services;

import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;
import parksys.entities.Veiculo;
import parksys.enums.StatusVaga;
import parksys.enums.TipoVeiculo;
import parksys.exceptions.VagaOcupadaException;
import parksys.exceptions.VeiculoNaoEncontradoException;
import parksys.observer.EstacionamentoObserver;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GerenciadorEstacionamento implements Serializable {
    private static GerenciadorEstacionamento instancia;
    private ArrayList<Registro> registros;
    private HashMap<String, Vaga> vagas;
    private static final long serialVersionUID = 1L;
    
    // C03: LinkedList para mensalistas
    private LinkedList<Mensalista> mensalistas;
    
    // P03: Observer pattern
    private transient List<EstacionamentoObserver> observadores;

    private GerenciadorEstacionamento() {
        registros = new ArrayList<>();
        vagas = new HashMap<>();
        mensalistas = new LinkedList<>();
        observadores = new LinkedList<>();
        inicializarVagas();
    }

    private void inicializarVagas() {
        for (char fileira : new char[]{'A', 'B'}) {
            for (int i = 1; i <= 15; i++) {
                String id = String.format("%c%02d", fileira, i);
                vagas.put(id, new Vaga(id));
            }
        }
    }

    // M03: Adicionado synchronized em todos os métodos que leem ou alteram as coleções (vagas e registros).
    // Explicação do risco de race condition sem synchronized:
    // Sem o uso de synchronized, duas ou mais threads poderiam acessar e alterar o estado das coleções 
    // (como o HashMap de vagas ou o ArrayList de registros) ao mesmo tempo. Isso pode causar inconsistências,
    // como duas threads alocando a mesma vaga simultaneamente, ou sobrescrita silenciosa de registros 
    // em estruturas que não são thread-safe nativamente. O synchronized garante que apenas uma thread
    // por vez possa executar os métodos que alteram o estado compartilhado (locks intrínsecos do objeto).
    public synchronized boolean registrarEntrada(String placa, TipoVeiculo tipo, String idVagaPreferida) {
        Vaga vagaPref = vagas.get(idVagaPreferida);

        for (Registro r : registros) {
            if (r.getVeiculo().getPlaca().equals(placa) && r.getDataHoraSaida() == null) {
                System.out.println("Veículo já está no estacionamento!");
                return false;
            }
        }

        if (vagaPref == null) {
            System.out.println("Vaga " + idVagaPreferida + " não existe");
            return false;
        }

        int vagasNecessarias = tipo.getVagasOcupadas();
        List<String> vagasParaOcupar = new ArrayList<>();

        if (vagasNecessarias == 1) {
            if (vagaPref.getStatusVaga() == StatusVaga.LIVRE) {
                vagasParaOcupar.add(idVagaPreferida);
            } else {
                throw new VagaOcupadaException("Vaga ocupada!");
            }
        } else {
            vagasParaOcupar = encontrarVagasConsecutivas(vagasNecessarias, idVagaPreferida);
            if (vagasParaOcupar == null) {
                System.out.println("Não há " + vagasNecessarias + " vagas consecutivas a partir de " + idVagaPreferida);
                return false;
            }
        }

        Veiculo veiculo = new Veiculo(placa, tipo);
        Registro registro = new Registro(veiculo);
        registro.setThreadOrigem(Thread.currentThread().getName());
        registros.add(registro);

        for (String idVaga : vagasParaOcupar) {
            Vaga vaga = vagas.get(idVaga);
            vaga.setStatusVaga(StatusVaga.OCUPADA);
            vaga.setVeiculo(veiculo);
            notificarObservadores(idVaga, StatusVaga.OCUPADA);
        }

        System.out.println("Entrada registrada: " + placa + " nas vagas " + vagasParaOcupar);
        return true;
    }

    private List<String> encontrarVagasConsecutivas(int quantidade, String idInicio) {
        char fileira = idInicio.charAt(0);
        int numero;
        try {
            numero = Integer.parseInt(idInicio.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            int numAtual = numero + i;
            if (numAtual > 15) {
                return null;
            }
            String id = String.format("%c%02d", fileira, numAtual);
            Vaga vaga = vagas.get(id);
            if (vaga == null || vaga.getStatusVaga() != StatusVaga.LIVRE) {
                return null;
            }
            ids.add(id);
        }
        return ids;
    }

    // M03: synchronized para evitar race condition
    public synchronized double registrarSaida(String placa) {
        Registro registro = null;

        for (Registro r : registros) {
            if (r.getVeiculo().getPlaca().equals(placa) && r.getDataHoraSaida() == null) {
                registro = r;
                break;
            }
        }

        if (registro == null) {
            throw new VeiculoNaoEncontradoException("Veículo com a placa " + placa + " não encontrado ou já saiu.");
        }

        LocalDateTime agora = LocalDateTime.now();
        long minutosEstacionado = java.time.Duration.between(registro.getDataHoraEntrada(), agora).toMinutes();
        int horasEstacionado = (int) Math.ceil(minutosEstacionado / 60.0);
        if (horasEstacionado == 0) horasEstacionado = 1; // Mínimo 1 hora

        double tarifaHora = registro.getVeiculo().getTipoVeiculo().getTarifaHora();
        double valorPago = tarifaHora * horasEstacionado;

        registro.setValorPago(valorPago);
        registro.setDataHoraSaida(agora);

        for (Vaga vaga : vagas.values()) {
            if (vaga.getVeiculo() != null && vaga.getVeiculo().getPlaca().equals(placa)) {
                vaga.setStatusVaga(StatusVaga.LIVRE);
                vaga.setVeiculo(null);
                notificarObservadores(vaga.getId(), StatusVaga.LIVRE);
            }
        }

        System.out.println("Saída registrada: " + placa + " - Valor: R$ " + String.format("%.2f", valorPago) + " - Horas: " + horasEstacionado);
        return valorPago;
    }

    // C03: getter de mensalistas com justificativa em comentário
    // O uso de LinkedList é adequado para a lista de mensalistas pois ela oferece complexidade O(1)
    // para inserção e remoção em ambas as pontas da lista, o que ocorre com frequência ao gerenciar cadastros.
    public synchronized LinkedList<Mensalista> getMensalistas() {
        return mensalistas;
    }

    // Cadastra um mensalista e reserva a vaga correspondente
    public synchronized boolean cadastrarMensalista(String placa, TipoVeiculo tipoVeiculo, String nome, double mensalidade, LocalDate validade, String idVaga) {
        Vaga vaga = vagas.get(idVaga);
        if (vaga == null) {
            return false;
        }
        if (vaga.getStatusVaga() != StatusVaga.LIVRE) {
            throw new VagaOcupadaException("Vaga " + idVaga + " já está ocupada/reservada!");
        }

        Mensalista mensalista = new Mensalista(placa, tipoVeiculo, nome, mensalidade, validade);
        mensalistas.add(mensalista);

        vaga.setStatusVaga(StatusVaga.RESERVADA);
        vaga.setVeiculo(mensalista);
        notificarObservadores(idVaga, StatusVaga.RESERVADA);

        return true;
    }

    // C04: TreeSet para registros ordenados cronologicamente
    public synchronized TreeSet<Registro> getRegistrosOrdenados() {
        return new TreeSet<>(registros);
    }

    // C05: obterRelatorioReceitaDecrescente
    public synchronized List<Registro> obterRelatorioReceitaDecrescente() {
        return registros.stream()
                .filter(r -> r.getDataHoraSaida() != null)
                .sorted(Comparator.comparingDouble(Registro::getValorPago).reversed())
                .collect(Collectors.toList());
    }

    // C06: Relatório usando entrySet() do HashMap e for-each
    public synchronized String gerarTextoRelatorio() {
        int livres = 0;
        int ocupadas = 0;
        int reservadas = 0;

        for (Map.Entry<String, Vaga> entry : vagas.entrySet()) {
            Vaga vaga = entry.getValue();
            if (vaga.getStatusVaga() == StatusVaga.LIVRE) {
                livres++;
            } else if (vaga.getStatusVaga() == StatusVaga.OCUPADA) {
                ocupadas++;
            } else if (vaga.getStatusVaga() == StatusVaga.RESERVADA) {
                reservadas++;
            }
        }

        double totalReceita = 0.0;
        for (Registro r : registros) {
            totalReceita += r.getValorPago();
        }

        LocalDate hoje = LocalDate.now();
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("         RELATÓRIO DO ESTACIONAMENTO    \n");
        sb.append("========================================\n");
        sb.append("Gerado em: ").append(LocalDateTime.now()).append("\n\n");
        sb.append("--- Ocupação das Vagas ---\n");
        sb.append("  Vagas Livres: ").append(livres).append("\n");
        sb.append("  Vagas Ocupadas: ").append(ocupadas).append("\n");
        sb.append("  Vagas Reservadas (Mensalistas): ").append(reservadas).append("\n\n");
        sb.append("--- Financeiro ---\n");
        sb.append(String.format("  Receita Total: R$ %.2f\n\n", totalReceita));
        sb.append("--- Registros do Dia (").append(hoje).append(") ---\n");

        boolean temRegistro = false;
        for (Registro r : registros) {
            if (r.getDataHoraEntrada().toLocalDate().equals(hoje)) {
                String saidaStr = r.getDataHoraSaida() != null ? r.getDataHoraSaida().toString() : "Em andamento";
                sb.append("  Placa: ").append(r.getVeiculo().getPlaca())
                  .append(" | Entrada: ").append(r.getDataHoraEntrada())
                  .append(" | Saída: ").append(saidaStr)
                  .append(" | Valor Pago: R$ ").append(String.format("%.2f", r.getValorPago())).append("\n");
                temRegistro = true;
            }
        }
        if (!temRegistro) {
            sb.append("  Nenhum veículo registrado hoje.\n");
        }
        sb.append("========================================");
        return sb.toString();
    }

    public synchronized Vaga getVaga(String id) {
        return vagas.get(id);
    }

    public synchronized HashMap<String, Vaga> getVagas() {
        return vagas;
    }

    public synchronized List<Registro> getRegistros() {
        return registros;
    }

    // P03: Observer pattern
    public synchronized void addObserver(EstacionamentoObserver observer) {
        if (observadores == null) {
            observadores = new LinkedList<>();
        }
        observadores.add(observer);
    }

    public synchronized void removeObserver(EstacionamentoObserver observer) {
        if (observadores != null) {
            observadores.remove(observer);
        }
    }

    private synchronized void notificarObservadores(String idVaga, StatusVaga novoStatus) {
        if (observadores != null) {
            for (EstacionamentoObserver observer : observadores) {
                observer.onVagaAlterada(idVaga, novoStatus);
            }
        }
    }

    protected Object readResolve() {
        GerenciadorEstacionamento singleton = getInstancia();
        singleton.registros = this.registros != null ? this.registros : new ArrayList<>();
        singleton.vagas = this.vagas != null ? this.vagas : new HashMap<>();
        singleton.mensalistas = this.mensalistas != null ? this.mensalistas : new LinkedList<>();
        singleton.observadores = new LinkedList<>();
        return singleton;
    }

    public static synchronized GerenciadorEstacionamento getInstancia() {
        if (instancia == null) {
            instancia = new GerenciadorEstacionamento();
        }
        return instancia;
    }
}

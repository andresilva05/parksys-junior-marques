package parksys.services;

import parksys.entities.Mensalista;
import parksys.entities.Registro;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import parksys.entities.Vaga;
import parksys.entities.Veiculo;
import parksys.enums.StatusVaga;
import parksys.enums.TipoVeiculo;
import parksys.exceptions.VagaOcupadaException;
import parksys.exceptions.VeiculoNaoEncontradoException;
import parksys.observer.EstacionamentoObserver;


public class GerenciadorEstacionamento implements Serializable {
    private static GerenciadorEstacionamento instancia;
    private ArrayList<Registro> registros;
    private HashMap<String, Vaga> vagas;
    private static final long serialVersionUID = 1L;
    private LinkedList<Mensalista> mensalistas;
    private LinkedList<EstacionamentoObserver> observadores;

    //Ordena  os registros automaticamente por data de entrada, do mais antigo pro mais recente.
    public TreeSet<Registro> getRegistrosOrdenados() {
        return new TreeSet<>(registros);
    }

    private GerenciadorEstacionamento() {
        registros = new ArrayList<>();
        vagas = new HashMap<>();
        inicializarVagas();
        observadores = new LinkedList<>();

        //Para mensalistas, não buscamos por índice — a gente adiciona e cancela mensalidade com frequência. Por isso LinkedList faz mais sentido.
        mensalistas = new LinkedList<>();
    }


    private void inicializarVagas() {
        for (char fileira : new char[]{'A', 'B'}) {
            for (int i = 1; i <= 15; i++) {
                String id = String.format("%c%02d", fileira, i);
                vagas.put(id, new Vaga(id));
            }
        }

    }

    //Sem o synchronized, não há garantia de que uma operação de "verificar e reservar" seja atômica (ou seja, concluída sem interrupções de outras threads)
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
            notificarObservadores(idVaga, StatusVaga.OCUPADA);
            vaga.setVeiculo(veiculo);
        }

        System.out.println("Entrada registrada: " + placa + " nas vagas " + vagasParaOcupar);
        return true;
    }

    //    return new TreeSet<>(registros);
    private void addObserver(EstacionamentoObserver observer) {
        observadores.add(observer);
    }

    public void removeObserver(EstacionamentoObserver observer) {
        observadores.remove(observer);
    }

    private void notificarObservadores(String idVaga, StatusVaga novoStatus) {
        // percorre a lista e chama onVagaAlterada() em cada observer
        for (EstacionamentoObserver observer : observadores) {
            observer.onVagaAlterada(idVaga, novoStatus);
        }
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

    //Sem o synchronized, não há garantia de que uma operação de "verificar e reservar" seja atômica (ou seja, concluída sem interrupções de outras threads)
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

        Veiculo veiculo = registro.getVeiculo();
        for (Vaga vaga : vagas.values()) {
            if (vaga.getVeiculo() != null && vaga.getVeiculo().getPlaca().equals(placa)) {
                vaga.setStatusVaga(StatusVaga.LIVRE);
                notificarObservadores(vaga.getId(), StatusVaga.LIVRE);
                vaga.setVeiculo(null);

            }
        }

        System.out.println("Saída registrada: " + placa + " - Valor: R$ " + String.format("%.2f", valorPago) + " - Horas: " + horasEstacionado);
        return valorPago;
    }

    public Vaga getVaga(String id) {
        return vagas.get(id);
    }

    public HashMap<String, Vaga> getVagas() {
        return vagas;
    }

    protected Object readResolve() {
        GerenciadorEstacionamento singleton = getInstancia();
        singleton.registros = this.registros;
        singleton.vagas = this.vagas;
        return singleton;
    }


    public static synchronized GerenciadorEstacionamento getInstancia() {
        if (instancia == null) {
            instancia = new GerenciadorEstacionamento();
        }
        return instancia;
    }

}

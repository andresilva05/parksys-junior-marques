package parksys.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Registro implements Serializable, Comparable<Registro> {

    private static final long serialVersionUID = 1L;
    private Veiculo veiculo;
    private double valorPago;
    private transient String threadOrigem;
    private LocalDateTime dataHoraEntrada;
    private LocalDateTime dataHoraSaida;

    @Override
    public int compareTo(Registro registroComparacao) {
        return this.dataHoraEntrada.compareTo(registroComparacao.dataHoraEntrada);
    }

    public Registro(Veiculo veiculo){
        this.veiculo = veiculo;
        this.dataHoraEntrada = LocalDateTime.now();
    }


    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public double getValorPago() {
        return valorPago;
    }

    public void setValorPago(double valorPago) {
        this.valorPago = valorPago;
    }

    public String getThreadOrigem() {
        return threadOrigem;
    }

    public void setThreadOrigem(String threadOrigem) {
        this.threadOrigem = threadOrigem;
    }

    public LocalDateTime getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) {
        this.dataHoraEntrada = dataHoraEntrada;
    }

    public LocalDateTime getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(LocalDateTime dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }

    @Override
    public String toString() {
        return veiculo.toString() + " - Entrada: " + dataHoraEntrada;
    }
}

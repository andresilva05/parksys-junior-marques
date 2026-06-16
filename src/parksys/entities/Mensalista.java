package parksys.entities;

import parksys.enums.TipoVeiculo;

import java.io.Serializable;
import java.time.LocalDate;

public class Mensalista extends Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    private double mensalidade;
    private LocalDate validade;


    public Mensalista(String placa, TipoVeiculo tipoVeiculo, String nome, double mensalidade, LocalDate validade) {
        super(placa, tipoVeiculo);
        this.nome = nome;
        this.mensalidade = mensalidade;
        this.validade = validade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getMensalidade() {
        return mensalidade;
    }

    public void setMensalidade(double mensalidade) {
        this.mensalidade = mensalidade;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    @Override
    public String toString() {
        return nome + " - " + getPlaca() + " - Valor: " + mensalidade + "\n" + "Válido até: " + validade;
    }
}

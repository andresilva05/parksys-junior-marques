package parksys.entities;

import parksys.enums.TipoVeiculo;

import java.io.Serializable;

public class Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String placa;
    private TipoVeiculo tipoVeiculo;

    public Veiculo(String placa, TipoVeiculo tipoVeiculo){
        this.tipoVeiculo = tipoVeiculo;
        this.placa = placa;
    }

    public TipoVeiculo getTipoVeiculo() {
        return tipoVeiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public void setTipoVeiculo(TipoVeiculo tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    @Override
    public String toString() {
        return placa + " - " + tipoVeiculo.getNome();
    }
}

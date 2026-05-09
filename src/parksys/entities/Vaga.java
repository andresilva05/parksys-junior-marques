package parksys.entities;

import parksys.enums.StatusVaga;

import java.io.Serializable;

public class Vaga implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;
        private StatusVaga statusVaga;
        private Veiculo veiculo;

        public Vaga(String id){
            this.id = id;
            this.statusVaga = StatusVaga.LIVRE;
            this.veiculo = null;
        }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StatusVaga getStatusVaga() {
        return statusVaga;
    }

    public void setStatusVaga(StatusVaga statusVaga) {
        this.statusVaga = statusVaga;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    @Override
    public String toString() {
        return id + " - " + statusVaga.getDescricao();
    }
}

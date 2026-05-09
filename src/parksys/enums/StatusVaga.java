package parksys.enums;

public enum StatusVaga {
    LIVRE("Livre", true),
    OCUPADA("Ocupada", false),
    RESERVADA("Reservada", false);

    private String descricao;
    private boolean disponivel;

    StatusVaga(String descricao, boolean disponivel){
        this.descricao = descricao;
        this.disponivel = disponivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isDisponivel() {
        return disponivel;
    }
    @Override
    public String toString() {
        return descricao;
    }
}

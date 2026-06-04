package parksys.main;

import parksys.services.GerenciadorEstacionamento;
import parksys.services.GerenciadorArquivo;

public class Main {
    public static void main(String[] args) {
        GerenciadorEstacionamento gerenciador = GerenciadorArquivo.desserializar();

        if (gerenciador == null) {
            gerenciador = GerenciadorEstacionamento.getInstancia();
        }

        System.out.println("Aplicação iniciada com sucesso!");
    }
}
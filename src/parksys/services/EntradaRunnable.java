package parksys.services;

import parksys.enums.TipoVeiculo;

public class EntradaRunnable implements Runnable{
    String placa;
    TipoVeiculo tipo;
    String idVagaDesejada;
    GerenciadorEstacionamento gerenciador;

    public EntradaRunnable(String placa, TipoVeiculo tipo, String idVagaDesejada, GerenciadorEstacionamento instancia){
        this.placa = placa;
        this.idVagaDesejada = idVagaDesejada;
        this.tipo = tipo;
        this.gerenciador = instancia;
    }
    @Override
    public void run() {
        try{
            System.out.println("Thread criada com sucesso!");
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        boolean sucesso = gerenciador.registrarEntrada(placa, tipo, idVagaDesejada);
        if (sucesso) {
            System.out.println("Entrada registrada com sucesso para " + placa);
        } else {
            System.out.println("Falha ao registrar entrada de " + placa);
        }

    }
}

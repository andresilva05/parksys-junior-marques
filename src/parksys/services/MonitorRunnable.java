package parksys.services;

import parksys.entities.Vaga;
import parksys.enums.StatusVaga;

import java.util.Map;

public class MonitorRunnable implements Runnable {
    private final GerenciadorEstacionamento gerenciador;

    public MonitorRunnable(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
    }

    @Override
    public void run() {
        System.out.println("[Monitor] Iniciando monitoramento em daemon...");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int livres = 0;
                int ocupadas = 0;
                int reservadas = 0;

                // Lê de forma sincronizada do gerenciador
                Map<String, Vaga> vagas = gerenciador.getVagas();
                
                // M06: a cada 1 segundo imprime quantas vagas estão livres, ocupadas e reservadas
                synchronized (gerenciador) {
                    for (Vaga v : vagas.values()) {
                        if (v.getStatusVaga() == StatusVaga.LIVRE) livres++;
                        else if (v.getStatusVaga() == StatusVaga.OCUPADA) ocupadas++;
                        else if (v.getStatusVaga() == StatusVaga.RESERVADA) reservadas++;
                    }
                }

                System.out.printf("[Monitor] Vagas -> Livres: %d | Ocupadas: %d | Reservadas: %d%n", livres, ocupadas, reservadas);

                // Pausa de 1 segundo
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("[Monitor] Monitor interrompido. Encerrando.");
            // Preserva o status de interrupção
            Thread.currentThread().interrupt();
        }
    }
}

package parksys.main;

import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import parksys.ui.TelaInicial;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Carrega dados persistidos ou cria novo gerenciador
        GerenciadorEstacionamento gerenciador = GerenciadorArquivo.desserializar();
        if (gerenciador == null) {
            gerenciador = GerenciadorEstacionamento.getInstancia();
        }

        // M07: Após desserializar, imprimir o campo threadOrigem de cada Registro.
        // Como 'threadOrigem' foi marcada com a palavra-chave 'transient', seu valor não é salvo 
        // durante o processo de serialização (escrita no arquivo). Logo, ao desserializar, ela recebe
        // o valor padrão do tipo de dado (null para referências de objetos como String).
        System.out.println("\n--- Verificação do campo transient threadOrigem (M07) ---");
        for (parksys.entities.Registro r : gerenciador.getRegistros()) {
            System.out.println("Placa: " + r.getVeiculo().getPlaca() + " | threadOrigem: " + r.getThreadOrigem());
        }
        System.out.println("----------------------------------------------------------\n");

        // M06: Criar MonitorRunnable (daemon) que imprime vagas a cada 1 segundo
        Thread monitorThread = new Thread(new parksys.services.MonitorRunnable(gerenciador), "Monitor-Vagas");
        monitorThread.setDaemon(true); // Marca como daemon ANTES do start()
        monitorThread.start();

        // M05: Criar mínimo de 4 threads e aguardar todas com join() antes de exibir o relatório
        System.out.println("Iniciando simulação de entradas via Threads (M05)...");
        Thread t1 = new Thread(new parksys.services.EntradaRunnable("M05-AAA1", parksys.enums.TipoVeiculo.CARRO, "A01", gerenciador), "Entrada-1");
        Thread t2 = new Thread(new parksys.services.EntradaRunnable("M05-BBB2", parksys.enums.TipoVeiculo.MOTO, "B02", gerenciador), "Entrada-2");
        Thread t3 = new Thread(new parksys.services.EntradaRunnable("M05-CCC3", parksys.enums.TipoVeiculo.SUV, "A03", gerenciador), "Entrada-3"); // Ocupará A03 e A04
        Thread t4 = new Thread(new parksys.services.EntradaRunnable("M05-DDD4", parksys.enums.TipoVeiculo.CARRO, "B05", gerenciador), "Entrada-4");

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            System.err.println("A simulação foi interrompida.");
        }

        // Encerrar o monitor após a conclusão das threads
        monitorThread.interrupt();

        // Exibir relatório final no console
        System.out.println("\nRelatório Pós-Simulação:\n");
        System.out.println(gerenciador.gerarTextoRelatorio());

        // Boa prática Swing: toda atualização de UI deve ocorrer na Event Dispatch Thread (EDT)
        final GerenciadorEstacionamento g = gerenciador;
        SwingUtilities.invokeLater(() -> {
            try {
                // Usa o Look and Feel do sistema operacional para aparência nativa
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            TelaInicial tela = new TelaInicial(g);
            tela.setVisible(true);
        });
    }
}
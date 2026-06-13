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

        System.out.println("Aplicação iniciada com sucesso!");

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
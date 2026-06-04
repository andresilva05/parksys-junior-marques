package parksys.observer;

import parksys.entities.Vaga;
import parksys.enums.StatusVaga;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;

/**
 * P04 - PainelMonitor
 *
 * Implementa a interface EstacionamentoObserver e exibe uma janela com o status
 * de todas as vagas em tempo real. Quando uma vaga muda de status, a tabela é
 * atualizada automaticamente via onVagaAlterada().
 */
public class PainelMonitor extends JFrame implements EstacionamentoObserver {

    private JTable tabelaVagas;
    private DefaultTableModel modeloTabela;
    private GerenciadorEstacionamento gerenciador;

    public PainelMonitor() {
        this.gerenciador = GerenciadorEstacionamento.getInstancia();

        // Configuração da janela
        setTitle("Monitor de Vagas - ParkSys");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Criação da tabela
        String[] colunas = {"ID Vaga", "Status", "Placa do Veículo"};
        modeloTabela = new DefaultTableModel(colunas, 0);
        tabelaVagas = new JTable(modeloTabela);
        tabelaVagas.setEnabled(false); // Apenas visualização

        // Scroll para a tabela
        JScrollPane scrollPane = new JScrollPane(tabelaVagas);
        add(scrollPane, BorderLayout.CENTER);

        // Popula a tabela com as vagas atuais
        atualizarTabela();

        setVisible(true);
    }

    /**
     * Atualiza a tabela inteira com o status atual de todas as vagas
     */
    private void atualizarTabela() {
        modeloTabela.setRowCount(0); // Limpa a tabela

        HashMap<String, Vaga> vagas = gerenciador.getVagas();

        for (String idVaga : vagas.keySet()) {
            Vaga vaga = vagas.get(idVaga);
            String placa = vaga.getVeiculo() != null ? vaga.getVeiculo().getPlaca() : "-";

            Object[] linha = {
                    idVaga,
                    vaga.getStatusVaga().getDescricao(),
                    placa
            };

            modeloTabela.addRow(linha);
        }
    }

    /**
     * P04 - Implementação do Observer
     * Chamado automaticamente quando uma vaga muda de status
     */
    @Override
    public void onVagaAlterada(String idVaga, StatusVaga novoStatus) {
        // Encontra a linha da vaga e atualiza
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            if (modeloTabela.getValueAt(i, 0).equals(idVaga)) {
                Vaga vagaAtualizada = gerenciador.getVaga(idVaga);
                String placa = vagaAtualizada.getVeiculo() != null
                        ? vagaAtualizada.getVeiculo().getPlaca()
                        : "-";

                modeloTabela.setValueAt(novoStatus.getDescricao(), i, 1);
                modeloTabela.setValueAt(placa, i, 2);
                break;
            }
        }
    }
}
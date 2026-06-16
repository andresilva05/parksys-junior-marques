package parksys.ui;

import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Tela Swing para visualização do Relatório Geral do Estacionamento (C06).
 * Segue o padrão MVC (P05) e a identidade visual de EstiloUI.
 */
public class TelaRelatorio extends JFrame {

    private JTextArea areaRelatorio;
    private final GerenciadorEstacionamento gerenciador;

    public TelaRelatorio(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
        configurarJanela();
        inicializarComponentes();
        atualizarRelatorio();
        configurarSalvarAoFechar();
    }

    private void configurarJanela() {
        setTitle("ParkSys — Relatório Geral");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloUI.COR_FUNDO);
        setLayout(new BorderLayout(0, 0));
    }

    private void inicializarComponentes() {
        add(criarPainelTitulo(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
        add(criarPainelBotoes(), BorderLayout.SOUTH);
    }

    private JPanel criarPainelTitulo() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 16));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloUI.COR_BORDA));

        JLabel titulo = new JLabel("Relatório do Estacionamento");
        titulo.setFont(EstiloUI.FONTE_TITULO);
        titulo.setForeground(EstiloUI.COR_TEXTO);

        painel.add(titulo);
        return painel;
    }

    private JPanel criarPainelCentral() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(EstiloUI.COR_FUNDO);
        wrapper.setBorder(new EmptyBorder(16, 20, 16, 20));

        areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(EstiloUI.FONTE_RESULT);
        areaRelatorio.setBackground(new Color(15, 15, 25));
        areaRelatorio.setForeground(EstiloUI.COR_TEXTO);
        areaRelatorio.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(areaRelatorio);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 60), 1));
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel criarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.COR_BORDA));

        JButton btnExportar = criarBotao("Exportar .txt", EstiloUI.COR_BOTAO_LIMPAR);
        btnExportar.addActionListener(e -> exportarRelatorio());

        JButton btnRecarregar = criarBotao("Recarregar", EstiloUI.COR_BOTAO_CONF);
        btnRecarregar.addActionListener(e -> atualizarRelatorio());

        painel.add(btnExportar);
        painel.add(btnRecarregar);
        return painel;
    }

    private void atualizarRelatorio() {
        String textoReport = gerenciador.gerarTextoRelatorio();
        areaRelatorio.setText(textoReport);
        areaRelatorio.setCaretPosition(0);
    }

    private void exportarRelatorio() {
        try {
            String caminho = "relatorio_estacionamento.txt";
            GerenciadorArquivo escritor = new GerenciadorArquivo();
            // Exporta os registros com o método de GerenciadorArquivo
            escritor.exportarRelatorioTxt(gerenciador.getRegistros(), caminho);
            JOptionPane.showMessageDialog(this, 
                    "Relatório exportado com sucesso para:\n" + caminho, 
                    "Exportação Concluída", 
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Erro ao exportar relatório: " + ex.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton criarBotao(String texto, Color corFundo) {
        JButton btn = new JButton(texto);
        btn.setFont(EstiloUI.FONTE_BOTAO);
        btn.setForeground(Color.WHITE);
        btn.setBackground(corFundo);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            final Color original = corFundo;
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(original.brighter());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }

    private void configurarSalvarAoFechar() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    GerenciadorArquivo.serializar(gerenciador);
                    JOptionPane.showMessageDialog(TelaRelatorio.this, "Dados salvos automaticamente ao fechar TelaRelatorio.", "ParkSys", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(TelaRelatorio.this, "Erro ao salvar dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

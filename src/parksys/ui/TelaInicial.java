package parksys.ui;

import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import parksys.observer.PainelMonitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * P06 - TelaInicial
 *
 * Tela inicial do sistema ParkSys que funciona como menu principal e gerencia
 * o ciclo de vida do PainelMonitor como observador do GerenciadorEstacionamento.
 */
public class TelaInicial extends JFrame {

    private final GerenciadorEstacionamento gerenciador;
    private PainelMonitor painelMonitor;

    public TelaInicial(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
        configurarJanela();
        inicializarComponentes();
        registrarObserver();
        configurarSalvarAoFechar();
    }

    private void configurarJanela() {
        setTitle("ParkSys — Sistema de Gestão de Estacionamento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 420);
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloUI.COR_FUNDO);
        setLayout(new BorderLayout(0, 0));
    }

    private void inicializarComponentes() {
        // Painel Superior com Título
        JPanel painelTitulo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        painelTitulo.setBackground(EstiloUI.COR_PAINEL);
        painelTitulo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloUI.COR_BORDA));
        
        JLabel titulo = new JLabel("ParkSys - Estacionamento");
        titulo.setFont(EstiloUI.FONTE_TITULO);
        titulo.setForeground(EstiloUI.COR_TEXTO);
        painelTitulo.add(titulo);
        add(painelTitulo, BorderLayout.NORTH);

        // Painel Central com Botões de Menu
        JPanel painelBotoes = new JPanel(new GridBagLayout());
        painelBotoes.setBackground(EstiloUI.COR_FUNDO);
        painelBotoes.setBorder(new EmptyBorder(20, 50, 20, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JButton btnEntrada = criarBotaoMenu("Registrar Entrada");
        btnEntrada.addActionListener(e -> {
            TelaRegistroEntrada tela = new TelaRegistroEntrada(gerenciador);
            tela.setVisible(true);
        });
        gbc.gridy = 0;
        painelBotoes.add(btnEntrada, gbc);

        JButton btnSaida = criarBotaoMenu("Registrar Saída");
        btnSaida.addActionListener(e -> {
            TelaSaida tela = new TelaSaida(gerenciador);
            tela.setVisible(true);
        });
        gbc.gridy = 1;
        painelBotoes.add(btnSaida, gbc);

        JButton btnMensalista = criarBotaoMenu("Cadastrar Mensalista");
        btnMensalista.addActionListener(e -> {
            TelaCadastroMensalista tela = new TelaCadastroMensalista(gerenciador);
            tela.setVisible(true);
        });
        gbc.gridy = 2;
        painelBotoes.add(btnMensalista, gbc);

        JButton btnRelatorio = criarBotaoMenu("Exibir Relatório");
        btnRelatorio.addActionListener(e -> {
            TelaRelatorio tela = new TelaRelatorio(gerenciador);
            tela.setVisible(true);
        });
        gbc.gridy = 3;
        painelBotoes.add(btnRelatorio, gbc);

        add(painelBotoes, BorderLayout.CENTER);
    }

    private JButton criarBotaoMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(EstiloUI.FONTE_BOTAO);
        btn.setForeground(Color.WHITE);
        btn.setBackground(EstiloUI.COR_BOTAO_CONF);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(12, 24, 12, 24));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(EstiloUI.COR_BORDA.brighter());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(EstiloUI.COR_BOTAO_CONF);
            }
        });
        return btn;
    }

    // P06 - Registrar PainelMonitor como observador do GerenciadorEstacionamento ao iniciar
    private void registrarObserver() {
        painelMonitor = new PainelMonitor();
        gerenciador.addObserver(painelMonitor);
    }

    // P06 - Ao fechar a aplicação, remover o observador antes de encerrar
    private void configurarSalvarAoFechar() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (painelMonitor != null) {
                    gerenciador.removeObserver(painelMonitor);
                    painelMonitor.dispose();
                }
                try {
                    GerenciadorArquivo.serializar(gerenciador);
                    System.out.println("Dados salvos automaticamente ao fechar TelaInicial.");
                } catch (IOException ex) {
                    System.err.println("Erro ao salvar dados: " + ex.getMessage());
                }
            }
        });
    }
}

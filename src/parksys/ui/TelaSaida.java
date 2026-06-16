package parksys.ui;

import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import parksys.exceptions.VeiculoNaoEncontradoException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Tela Swing para registrar a saída de veículos do estacionamento.
 * Exibe o valor pago calculado pela estadia (tarifa x horas).
 * Segue o padrão MVC (P05) e a identidade visual de EstiloUI.
 */
public class TelaSaida extends JFrame {

    private JTextField campoPlaca;
    private JTextArea areaResultado;
    private final GerenciadorEstacionamento gerenciador;

    public TelaSaida(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
        configurarJanela();
        inicializarComponentes();
        configurarSalvarAoFechar();
    }

    private void configurarJanela() {
        setTitle("ParkSys — Registro de Saída");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.COR_FUNDO);
        setLayout(new BorderLayout(0, 0));
    }

    private void inicializarComponentes() {
        add(criarPainelTitulo(), BorderLayout.NORTH);
        add(criarPainelFormulario(), BorderLayout.CENTER);
        add(criarPainelBotoes(), BorderLayout.SOUTH);
    }

    private JPanel criarPainelTitulo() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 16));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloUI.COR_BORDA));

        JLabel titulo = new JLabel("Registro de Saída");
        titulo.setFont(EstiloUI.FONTE_TITULO);
        titulo.setForeground(EstiloUI.COR_TEXTO);

        painel.add(titulo);
        return painel;
    }

    private JPanel criarPainelFormulario() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(EstiloUI.COR_FUNDO);
        wrapper.setBorder(new EmptyBorder(24, 32, 8, 32));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(EstiloUI.COR_FUNDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        grid.add(criarLabel("Placa do Veículo"), gbc);

        campoPlaca = criarCampoTexto("Ex: ABC-1234");
        gbc.gridy = 1;
        grid.add(campoPlaca, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(16, 0, 0, 0);
        grid.add(criarAreaResultado(), gbc);

        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel criarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.COR_BORDA));

        JButton btnLimpar = criarBotao("Limpar", EstiloUI.COR_BOTAO_LIMPAR);
        btnLimpar.addActionListener(e -> limparFormulario());

        JButton btnConfirmar = criarBotao("Confirmar Saída", EstiloUI.COR_BOTAO_CONF);
        btnConfirmar.addActionListener(e -> confirmarSaida());

        painel.add(btnLimpar);
        painel.add(btnConfirmar);
        return painel;
    }

    private void confirmarSaida() {
        String placa = campoPlaca.getText().trim().toUpperCase();

        if (placa.isEmpty()) {
            exibirMensagem("Informe a placa do veículo.", false);
            campoPlaca.requestFocus();
            return;
        }

        try {
            double valorPago = gerenciador.registrarSaida(placa);
            exibirMensagem(
                    "Saída registrada com sucesso!\n" +
                    "   Placa : " + placa + "\n" +
                    "   Valor Pago: R$ " + String.format("%.2f", valorPago),
                    true
            );
            campoPlaca.setText("");
        } catch (VeiculoNaoEncontradoException ex) {
            exibirMensagem("Veículo com placa " + placa + " não encontrado ou já saiu.", false);
        } catch (Exception ex) {
            exibirMensagem("Erro inesperado: " + ex.getMessage(), false);
        }
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(EstiloUI.FONTE_LABEL);
        label.setForeground(EstiloUI.COR_LABEL);
        label.setBorder(new EmptyBorder(0, 0, 2, 0));
        return label;
    }

    private JTextField criarCampoTexto(String placeholder) {
        JTextField campo = new JTextField();
        campo.setFont(EstiloUI.FONTE_CAMPO);
        campo.setForeground(EstiloUI.COR_TEXTO);
        campo.setBackground(EstiloUI.COR_CAMPO);
        campo.setCaretColor(EstiloUI.COR_TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloUI.COR_BOTAO_LIMPAR, 1, false),
                new EmptyBorder(8, 12, 8, 12)
        ));
        campo.setToolTipText(placeholder);

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(EstiloUI.COR_BORDA, 1, false),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(EstiloUI.COR_BOTAO_LIMPAR, 1, false),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return campo;
    }

    private JButton criarBotao(String texto, Color corFundo) {
        JButton btn = new JButton(texto);
        btn.setFont(EstiloUI.FONTE_BOTAO);
        btn.setForeground(Color.WHITE);
        btn.setBackground(corFundo);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 22, 10, 22));
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

    private JScrollPane criarAreaResultado() {
        areaResultado = new JTextArea(6, 30);
        areaResultado.setEditable(false);
        areaResultado.setFont(EstiloUI.FONTE_RESULT);
        areaResultado.setBackground(new Color(15, 15, 25));
        areaResultado.setForeground(EstiloUI.COR_LABEL);
        areaResultado.setBorder(new EmptyBorder(10, 12, 10, 12));
        areaResultado.setText("Aguardando registro...");
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 60), 1));
        return scroll;
    }

    private void exibirMensagem(String message, boolean sucesso) {
        areaResultado.setForeground(sucesso ? EstiloUI.COR_SUCESSO : EstiloUI.COR_ERRO);
        areaResultado.setText(message);
    }

    private void limparFormulario() {
        campoPlaca.setText("");
        areaResultado.setForeground(EstiloUI.COR_LABEL);
        areaResultado.setText("Aguardando registro...");
        campoPlaca.requestFocus();
    }

    private void configurarSalvarAoFechar() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    GerenciadorArquivo.serializar(gerenciador);
                    JOptionPane.showMessageDialog(TelaSaida.this, "Dados salvos automaticamente ao fechar TelaSaida.", "ParkSys", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(TelaSaida.this, "Erro ao salvar dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

package parksys.ui;

import parksys.enums.TipoVeiculo;
import parksys.exceptions.VagaOcupadaException;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Tela Swing para realizar o cadastro de clientes mensalistas (C03).
 * Segue o padrão MVC (P05) e a identidade visual de EstiloUI.
 */
public class TelaCadastroMensalista extends JFrame {

    private JTextField campoNome;
    private JTextField campoPlaca;
    private JTextField campoMensalidade;
    private JTextField campoValidade;
    private JTextField campoVaga;
    private JComboBox<TipoVeiculo> comboTipoVeiculo;
    private JTextArea areaResultado;
    private final GerenciadorEstacionamento gerenciador;

    public TelaCadastroMensalista(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
        configurarJanela();
        inicializarComponentes();
        configurarSalvarAoFechar();
    }

    private void configurarJanela() {
        setTitle("ParkSys — Cadastro de Mensalista");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(540, 680);
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

        JLabel titulo = new JLabel("Cadastro de Mensalista");
        titulo.setFont(EstiloUI.FONTE_TITULO);
        titulo.setForeground(EstiloUI.COR_TEXTO);

        painel.add(titulo);
        return painel;
    }

    private JPanel criarPainelFormulario() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(EstiloUI.COR_FUNDO);
        wrapper.setBorder(new EmptyBorder(20, 32, 8, 32));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(EstiloUI.COR_FUNDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1.0;

        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        grid.add(criarLabel("Nome Completo"), gbc);
        campoNome = criarCampoTexto("Ex: João da Silva");
        gbc.gridy = 1;
        grid.add(campoNome, gbc);

        // Placa
        gbc.gridy = 2;
        grid.add(criarLabel("Placa do Veículo"), gbc);
        campoPlaca = criarCampoTexto("Ex: ABC-1234");
        gbc.gridy = 3;
        grid.add(campoPlaca, gbc);

        // Tipo Veículo
        gbc.gridy = 4;
        grid.add(criarLabel("Tipo de Veículo"), gbc);
        comboTipoVeiculo = new JComboBox<>(TipoVeiculo.values());
        estilizarCombo(comboTipoVeiculo);
        gbc.gridy = 5;
        grid.add(comboTipoVeiculo, gbc);

        // Mensalidade
        gbc.gridy = 6;
        grid.add(criarLabel("Valor da Mensalidade (R$)"), gbc);
        campoMensalidade = criarCampoTexto("Ex: 150.00");
        gbc.gridy = 7;
        grid.add(campoMensalidade, gbc);

        // Validade
        gbc.gridy = 8;
        grid.add(criarLabel("Validade (DD/MM/AAAA)"), gbc);
        campoValidade = criarCampoTexto("Ex: 31/12/2026");
        gbc.gridy = 9;
        grid.add(campoValidade, gbc);

        // Vaga
        gbc.gridy = 10;
        grid.add(criarLabel("Código da Vaga Reservada"), gbc);
        campoVaga = criarCampoTexto("Ex: A05");
        gbc.gridy = 11;
        grid.add(campoVaga, gbc);

        // Área Resultado
        gbc.gridy = 12;
        gbc.insets = new Insets(12, 0, 0, 0);
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

        JButton btnConfirmar = criarBotao("Confirmar Cadastro", EstiloUI.COR_BOTAO_CONF);
        btnConfirmar.addActionListener(e -> confirmarCadastro());

        painel.add(btnLimpar);
        painel.add(btnConfirmar);
        return painel;
    }

    private void confirmarCadastro() {
        String nome = campoNome.getText().trim();
        String placa = campoPlaca.getText().trim().toUpperCase();
        TipoVeiculo tipo = (TipoVeiculo) comboTipoVeiculo.getSelectedItem();
        String mensalidadeStr = campoMensalidade.getText().trim();
        String validadeStr = campoValidade.getText().trim();
        String vaga = campoVaga.getText().trim().toUpperCase();

        if (nome.isEmpty() || placa.isEmpty() || mensalidadeStr.isEmpty() || validadeStr.isEmpty() || vaga.isEmpty() || tipo == null) {
            exibirMensagem("Todos os campos devem ser preenchidos.", false);
            return;
        }

        double mensalidade;
        try {
            mensalidade = Double.parseDouble(mensalidadeStr);
        } catch (NumberFormatException e) {
            exibirMensagem("Mensalidade inválida. Digite apenas números (ex: 150.00).", false);
            return;
        }

        LocalDate validade;
        try {
            validade = LocalDate.parse(validadeStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            exibirMensagem("Data de validade inválida. Use o formato DD/MM/AAAA.", false);
            return;
        }

        try {
            boolean sucesso = gerenciador.cadastrarMensalista(placa, tipo, nome, mensalidade, validade, vaga);
            if (sucesso) {
                exibirMensagem(
                        "Mensalista cadastrado com sucesso!\n" +
                        "   Nome  : " + nome + "\n" +
                        "   Placa : " + placa + "\n" +
                        "   Vaga  : " + vaga + " (Status: RESERVADA)",
                        true
                );
                limparFormulario();
            } else {
                exibirMensagem("Não foi possível cadastrar o mensalista. Verifique se a vaga existe ou se está livre.", false);
            }
        } catch (VagaOcupadaException ex) {
            exibirMensagem("Vaga " + vaga + " já está ocupada ou reservada!", false);
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
                new EmptyBorder(6, 12, 6, 12)
        ));
        campo.setToolTipText(placeholder);

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(EstiloUI.COR_BORDA, 1, false),
                        new EmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(EstiloUI.COR_BOTAO_LIMPAR, 1, false),
                        new EmptyBorder(6, 12, 6, 12)
                ));
            }
        });
        return campo;
    }

    private void estilizarCombo(JComboBox<TipoVeiculo> combo) {
        combo.setFont(EstiloUI.FONTE_CAMPO);
        combo.setForeground(EstiloUI.COR_TEXTO);
        combo.setBackground(EstiloUI.COR_CAMPO);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloUI.COR_BOTAO_LIMPAR, 1, false),
                new EmptyBorder(2, 6, 2, 6)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(EstiloUI.FONTE_CAMPO);
                setBackground(isSelected ? EstiloUI.COR_BORDA : EstiloUI.COR_CAMPO);
                setForeground(EstiloUI.COR_TEXTO);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                if (value instanceof TipoVeiculo tv) {
                    setText(tv.getNome());
                }
                return this;
            }
        });
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
        areaResultado = new JTextArea(4, 30);
        areaResultado.setEditable(false);
        areaResultado.setFont(EstiloUI.FONTE_RESULT);
        areaResultado.setBackground(new Color(15, 15, 25));
        areaResultado.setForeground(EstiloUI.COR_LABEL);
        areaResultado.setBorder(new EmptyBorder(10, 12, 10, 12));
        areaResultado.setText("Aguardando cadastro...");
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 60), 1));
        return scroll;
    }

    private void exibirMensagem(String mensagem, boolean sucesso) {
        areaResultado.setForeground(sucesso ? EstiloUI.COR_SUCESSO : EstiloUI.COR_ERRO);
        areaResultado.setText(mensagem);
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoPlaca.setText("");
        campoMensalidade.setText("");
        campoValidade.setText("");
        campoVaga.setText("");
        comboTipoVeiculo.setSelectedIndex(0);
        areaResultado.setForeground(EstiloUI.COR_LABEL);
        areaResultado.setText("Aguardando cadastro...");
    }

    private void configurarSalvarAoFechar() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    GerenciadorArquivo.serializar(gerenciador);
                    JOptionPane.showMessageDialog(TelaCadastroMensalista.this, "Dados salvos automaticamente ao fechar TelaCadastroMensalista.", "ParkSys", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(TelaCadastroMensalista.this, "Erro ao salvar dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

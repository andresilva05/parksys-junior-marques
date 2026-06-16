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

/**
 * T05 — ComboBox com TipoVeiculo.values()
 *
 * Tela Swing para registrar a entrada de um veículo no estacionamento.
 * O JComboBox é populado automaticamente com TipoVeiculo.values(),
 * eliminando a necessidade de adicionar itens manualmente um por um.
 *
 * Toda a estilização (cores e fontes) foi extraída para {@link EstiloUI}.
 */
public class TelaRegistroEntrada extends JFrame {

    // ── Componentes de entrada ────────────────────────────────────────────────
    private JTextField campoPLaca;
    private JTextField campoVaga;

    /**
     * T05 — PONTO CENTRAL DA TAREFA
     * O tipo genérico <TipoVeiculo> garante que getSelectedItem() devolva
     * diretamente um TipoVeiculo, sem necessidade de cast inseguro.
     */
    private JComboBox<TipoVeiculo> comboTipoVeiculo;

    // ── Área de resultado ─────────────────────────────────────────────────────
    private JTextArea areaResultado;

    // ── Serviço ───────────────────────────────────────────────────────────────
    private final GerenciadorEstacionamento gerenciador;

    // ─────────────────────────────────────────────────────────────────────────

    public TelaRegistroEntrada(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
        configurarJanela();
        inicializarComponentes();
        configurarSalvarAoFechar();
    }

    // ── Configuração geral da janela ──────────────────────────────────────────

    private void configurarJanela() {
        setTitle("ParkSys — Registro de Entrada");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.COR_FUNDO);
        setLayout(new BorderLayout(0, 0));
    }

    // ── Construção dos componentes ────────────────────────────────────────────

    private void inicializarComponentes() {
        add(criarPainelTitulo(),     BorderLayout.NORTH);
        add(criarPainelFormulario(), BorderLayout.CENTER);
        add(criarPainelBotoes(),     BorderLayout.SOUTH);
    }

    /** Faixa superior com título */
    private JPanel criarPainelTitulo() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 16));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloUI.COR_BORDA));

        JLabel titulo = new JLabel("Registro de Entrada");
        titulo.setFont(EstiloUI.FONTE_TITULO);
        titulo.setForeground(EstiloUI.COR_TEXTO);

        painel.add(titulo);
        return painel;
    }

    /** Formulário com os campos de entrada */
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

        // ── Campo: Placa ──────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 0;
        grid.add(criarLabel("Placa do Veículo"), gbc);

        campoPLaca = criarCampoTexto("Ex: ABC-1234");
        gbc.gridy = 1;
        grid.add(campoPLaca, gbc);

        // ── Campo: Tipo de Veículo (COMBOBOX — T05) ───────────────────────────
        gbc.gridy = 2;
        grid.add(criarLabel("Tipo de Veículo"), gbc);

        /*
         * T05 — TipoVeiculo.values() popula o ComboBox automaticamente.
         *
         * TipoVeiculo.values() retorna um array com todos os valores do enum:
         *   [MOTO, CARRO, SUV, CAMINHAO]
         *
         * O JComboBox chama toString() em cada item para exibir o texto.
         * Como TipoVeiculo.toString() retorna this.nome, o usuário verá:
         *   "Motocicleta", "Automóvel", "Caminhonete/SUV", "Caminhao"
         */
        comboTipoVeiculo = new JComboBox<>(TipoVeiculo.values());
        estilizarCombo(comboTipoVeiculo);
        gbc.gridy = 3;
        grid.add(comboTipoVeiculo, gbc);

        // ── Painel informativo de tarifa (atualizado ao mudar o combo) ────────
        JPanel painelTarifa = criarPainelTarifa();
        gbc.gridy = 4;
        grid.add(painelTarifa, gbc);

        // Atualiza a tarifa exibida sempre que o usuário muda o tipo
        comboTipoVeiculo.addActionListener(e -> atualizarPainelTarifa(painelTarifa));
        atualizarPainelTarifa(painelTarifa); // inicializa com o primeiro item

        // ── Campo: Vaga Preferida ─────────────────────────────────────────────
        gbc.gridy = 5;
        grid.add(criarLabel("Vaga Preferida"), gbc);

        campoVaga = criarCampoTexto("Ex: A01, B08...");
        gbc.gridy = 6;
        grid.add(campoVaga, gbc);

        // ── Área de resultado ─────────────────────────────────────────────────
        gbc.gridy = 7;
        gbc.insets = new Insets(16, 0, 0, 0);
        grid.add(criarAreaResultado(), gbc);

        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }

    /** Painel que exibe tarifa e vagas ocupadas do tipo selecionado */
    private JPanel criarPainelTarifa() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        painel.setBackground(new Color(24, 24, 40));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloUI.COR_BOTAO_LIMPAR, 1, true),
                new EmptyBorder(2, 4, 2, 4)
        ));
        painel.setName("painelTarifa");
        return painel;
    }

    /** Atualiza os badges de tarifa conforme o TipoVeiculo selecionado no combo */
    private void atualizarPainelTarifa(JPanel painel) {
        TipoVeiculo tipo = (TipoVeiculo) comboTipoVeiculo.getSelectedItem();
        painel.removeAll();

        if (tipo != null) {
            painel.add(criarBadge(
                    "R$ " + String.format("%.2f", tipo.getTarifaHora()) + "/h",
                    new Color(99, 102, 241, 60), EstiloUI.COR_BORDA
            ));
            painel.add(criarBadge(
                    tipo.getVagasOcupadas() + " vaga(s)",
                    new Color(52, 211, 153, 40), EstiloUI.COR_SUCESSO
            ));
        }

        painel.revalidate();
        painel.repaint();
    }

    /** Badge colorido para exibir informações rápidas */
    private JLabel criarBadge(String texto, Color fundo, Color borda) {
        JLabel badge = new JLabel(texto);
        badge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        badge.setForeground(EstiloUI.COR_TEXTO);
        badge.setOpaque(true);
        badge.setBackground(fundo);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borda, 1, true),
                new EmptyBorder(3, 10, 3, 10)
        ));
        return badge;
    }

    /** Painel inferior com os botões de ação */
    private JPanel criarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.COR_BORDA));

        JButton btnLimpar = criarBotao("Limpar", EstiloUI.COR_BOTAO_LIMPAR);
        btnLimpar.addActionListener(e -> limparFormulario());

        JButton btnConfirmar = criarBotao("Confirmar Entrada", EstiloUI.COR_BOTAO_CONF);
        btnConfirmar.addActionListener(e -> confirmarEntrada());

        painel.add(btnLimpar);
        painel.add(btnConfirmar);
        return painel;
    }

    // ── Ação principal: confirmar entrada ─────────────────────────────────────

    private void confirmarEntrada() {
        String placa = campoPLaca.getText().trim().toUpperCase();
        String vaga  = campoVaga.getText().trim().toUpperCase();

        // Recupera o TipoVeiculo diretamente do ComboBox — sem cast de String
        TipoVeiculo tipo = (TipoVeiculo) comboTipoVeiculo.getSelectedItem();

        // ── Validações básicas ────────────────────────────────────────────────
        if (placa.isEmpty()) {
            exibirMensagem("Informe a placa do veículo.", false);
            campoPLaca.requestFocus();
            return;
        }
        if (vaga.isEmpty()) {
            exibirMensagem("Informe a vaga preferida (ex: A01).", false);
            campoVaga.requestFocus();
            return;
        }
        if (tipo == null) {
            exibirMensagem("Selecione o tipo de veículo.", false);
            return;
        }

        // ── Chamada ao serviço ────────────────────────────────────────────────
        try {
            boolean sucesso = gerenciador.registrarEntrada(placa, tipo, vaga);
            if (sucesso) {
                exibirMensagem(
                        "Entrada registrada com sucesso!\n" +
                        "   Placa : " + placa + "\n" +
                        "   Tipo  : " + tipo.getNome() + "\n" +
                        "   Vaga  : " + vaga + "\n" +
                        "   Tarifa: R$ " + String.format("%.2f", tipo.getTarifaHora()) + "/h",
                        true
                );
                limparFormulario();
            } else {
                exibirMensagem("Nao foi possivel registrar a entrada.\nVerifique a vaga informada.", false);
            }
        } catch (VagaOcupadaException ex) {
            exibirMensagem("Vaga " + vaga + " esta ocupada!\nEscolha outra vaga.", false);
        } catch (Exception ex) {
            exibirMensagem("Erro inesperado: " + ex.getMessage(), false);
        }
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

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

        // Efeito de foco: borda muda de cor
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

    private void estilizarCombo(JComboBox<TipoVeiculo> combo) {
        combo.setFont(EstiloUI.FONTE_CAMPO);
        combo.setForeground(EstiloUI.COR_TEXTO);
        combo.setBackground(EstiloUI.COR_CAMPO);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloUI.COR_BOTAO_LIMPAR, 1, false),
                new EmptyBorder(4, 8, 4, 8)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(EstiloUI.FONTE_CAMPO);
                setBackground(isSelected ? EstiloUI.COR_BORDA : EstiloUI.COR_CAMPO);
                setForeground(EstiloUI.COR_TEXTO);
                setBorder(new EmptyBorder(6, 12, 6, 12));

                // Exibe o nome do enum via toString()
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

        // Efeito hover
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
        areaResultado.setText("Aguardando registro...");
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
        campoPLaca.setText("");
        campoVaga.setText("");
        comboTipoVeiculo.setSelectedIndex(0);
        areaResultado.setForeground(EstiloUI.COR_LABEL);
        areaResultado.setText("Aguardando registro...");
        campoPLaca.requestFocus();
    }

    /** S06 — Serializa os dados automaticamente ao fechar a janela */
    private void configurarSalvarAoFechar() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    GerenciadorArquivo.serializar(gerenciador);
                    JOptionPane.showMessageDialog(TelaRegistroEntrada.this, "Dados salvos automaticamente ao fechar TelaRegistroEntrada.", "ParkSys", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(TelaRegistroEntrada.this, "Erro ao salvar dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

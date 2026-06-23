package parksys.ui;

import java.awt.*;

/**
 * Tema visual centralizado do ParkSys.
 *
 * Todas as cores e fontes da aplicação ficam aqui.
 * Qualquer tela que precisar de estilo importa apenas esta classe,
 * e qualquer mudança visual global basta editar um único arquivo.
 *
 * Uso:
 *   campo.setBackground(EstiloUI.COR_CAMPO);
 *   label.setFont(EstiloUI.FONTE_LABEL);
 */
public final class EstiloUI {

    // Construtor privado: classe utilitária, não deve ser instanciada
    private EstiloUI() {}

    // ── Cores de fundo ────────────────────────────────────────────────────────
    public static final Color COR_FUNDO        = new Color(18, 18, 28);
    public static final Color COR_PAINEL       = new Color(30, 30, 46);
    public static final Color COR_CAMPO        = new Color(24, 24, 37);

    // ── Cores de destaque ─────────────────────────────────────────────────────
    public static final Color COR_BORDA        = new Color(99, 102, 241);
    public static final Color COR_BOTAO_CONF   = new Color(99, 102, 241);
    public static final Color COR_BOTAO_LIMPAR = new Color(55, 65, 81);

    // ── Cores de texto ────────────────────────────────────────────────────────
    public static final Color COR_TEXTO        = new Color(229, 231, 235);
    public static final Color COR_LABEL        = new Color(156, 163, 175);

    // ── Cores de feedback ─────────────────────────────────────────────────────
    public static final Color COR_SUCESSO      = new Color(52, 211, 153);
    public static final Color COR_ERRO         = new Color(248, 113, 113);

    // ── Fontes ────────────────────────────────────────────────────────────────
    public static final Font FONTE_TITULO  = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONTE_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONTE_CAMPO   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONTE_BOTAO   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONTE_RESULT  = new Font("Consolas", Font.PLAIN, 13);
}

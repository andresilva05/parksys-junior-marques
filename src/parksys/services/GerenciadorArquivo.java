package parksys.services;

import parksys.entities.Registro;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * S06 - SERIALIZAÇÃO AUTOMÁTICA AO FECHAR JANELAS
 *
 * Este gerenciador é responsável por salvar e carregar os dados do estacionamento.
 * Para implementar a serialização automática, cada tela Swing (TelaInicial, TelaRegistroEntrada, etc)
 * deve registrar um WindowListener que chame serializar() antes de fechar.
 *
 * EXEMPLO DE USO:
 *
 * Em uma classe de tela (ex: TelaInicial.java):
 *
 *     public TelaInicial() {
 *         // ... configuração da tela ...
 *
 *         // Adiciona listener para salvar dados ao fechar a janela
 *         this.addWindowListener(new java.awt.event.WindowAdapter() {
 *             @Override
 *             public void windowClosing(java.awt.event.WindowEvent e) {
 *                 try {
 *                     GerenciadorArquivo.serializar(GerenciadorEstacionamento.getInstancia());
 *                     System.out.println("Dados salvos automaticamente!");
 *                 } catch (IOException ex) {
 *                     System.err.println("Erro ao salvar dados: " + ex.getMessage());
 *                 }
 *             }
 *         });
 *     }
 *
 * Desta forma, toda vez que o usuário fechar uma janela, os dados são salvos automaticamente.
 */
public class GerenciadorArquivo {

    private static final String ARQUIVO_PADRAO = "parksys.ser";

    public static void serializar(GerenciadorEstacionamento dados, String path) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(dados);
            System.out.println("Dados salvos com sucesso em: " + path);
        } catch (IOException e) {
            System.err.println("Erro ao salvar os dados: " + e.getMessage());
            throw e;
        }
    }

    public void exportarRelatorioTxt(List<Registro> registros, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            // 1️⃣ CABEÇALHO
            writer.write("===== RELATÓRIO DE ESTACIONAMENTO =====");
            writer.newLine();
            writer.write("Data: " + java.time.LocalDateTime.now());
            writer.newLine();
            writer.newLine();

            // 2️⃣ REGISTROS com for-each
            for (Registro registro : registros) {
                writer.write("Placa Veículo: " + registro.getVeiculo().getPlaca());
                writer.newLine();
                writer.write("Data/hora entrada: " + registro.getDataHoraEntrada());
                writer.newLine();
                writer.write("Data/hora saída: " + registro.getDataHoraSaida());
                writer.newLine();
                writer.write("Valor pago: R$ " + registro.getValorPago());
                writer.newLine();
                writer.newLine();
            }

            // 3️⃣ TOTAIS
            int totalRegistros = registros.size();
            BigDecimal valueTotal = BigDecimal.valueOf(0.0);
            for (Registro registro : registros) {
                valueTotal = valueTotal.add(BigDecimal.valueOf(registro.getValorPago()));
            }

            writer.write("===== TOTAIS =====");
            writer.newLine();
            writer.write("Total de registros: " + totalRegistros);
            writer.newLine();
            writer.write("Receita total: R$ " + valueTotal);
            writer.newLine();
            writer.write("===== FIM DO RELATÓRIO =====");
        } catch (IOException e) {
            System.err.println("Erro ao exportar relatório: " + e.getMessage());
        } finally {
            System.out.println("Exportação de relatório concluída");
        }
    }

    public static GerenciadorEstacionamento desserializar(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            GerenciadorEstacionamento obj = (GerenciadorEstacionamento) ois.readObject();
            System.out.println("Dados carregados com sucesso de: " + path);
            return obj;
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado. Será criado um novo estacionamento.");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar os dados: " + e.getMessage());
            return null;
        }
    }

    public static void serializar(GerenciadorEstacionamento dados) throws IOException {
        serializar(dados, ARQUIVO_PADRAO);
    }

    public static GerenciadorEstacionamento desserializar() {
        return desserializar(ARQUIVO_PADRAO);
    }
}
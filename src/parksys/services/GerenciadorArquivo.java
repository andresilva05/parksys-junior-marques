package parksys.services;

import java.io.*;

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
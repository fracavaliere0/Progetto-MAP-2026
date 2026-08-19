package support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Crea training set temporanei condivisi dai casi di test. */
public final class TestData {

    private TestData() {}

    /**
     * Scrive il contenuto indicato in un file temporaneo.
     *
     * @param content contenuto del training set.
     * @return percorso assoluto del file creato.
     * @throws IOException se non è possibile creare il file.
     */
    public static String create(String content) throws IOException {
        Path file = Files.createTempFile("map-training-", ".dat");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        file.toFile().deleteOnExit();
        return file.toString();
    }

    /**
     * Crea un training set con tre righe e tre attributi esplicativi.
     *
     * @return percorso assoluto del file creato.
     * @throws IOException se non è possibile creare il file.
     */
    public static String createBoundaryData() throws IOException {
        return create(
            "@schema 3\n" +
            "@desc first A,B,C\n" +
            "@desc second low,middle,high\n" +
            "@desc third X,Y,Z\n" +
            "@target target\n" +
            "@data 3\n" +
            "C,high,Z,30\n" +
            "A,low,X,10\n" +
            "B,middle,Y,20\n"
        );
    }

    /**
     * Crea un training set adatto a generare uno split con tre rami.
     *
     * @return percorso assoluto del file creato.
     * @throws IOException se non è possibile creare il file.
     */
    public static String createTreeData() throws IOException {
        return create(
            "@schema 2\n" +
            "@desc first A,B,C\n" +
            "@desc second X,Y\n" +
            "@target target\n" +
            "@data 6\n" +
            "A,X,1\n" +
            "A,Y,3\n" +
            "B,X,10\n" +
            "B,Y,12\n" +
            "C,X,20\n" +
            "C,Y,22\n"
        );
    }

    /**
     * Crea un training set nel quale l'attributo assume un solo valore.
     *
     * @return percorso assoluto del file creato.
     * @throws IOException se non è possibile creare il file.
     */
    public static String createSingleValueData() throws IOException {
        return create(
            "@schema 1\n" +
            "@desc only A\n" +
            "@target target\n" +
            "@data 3\n" +
            "A,1\n" +
            "A,2\n" +
            "A,3\n"
        );
    }

    /**
     * Crea il training set minimo valido.
     *
     * @return percorso assoluto del file creato.
     * @throws IOException se non è possibile creare il file.
     */
    public static String createSingleExampleData() throws IOException {
        return create(
            "@schema 1\n" +
            "@desc only A\n" +
            "@target target\n" +
            "@data 1\n" +
            "A,5\n"
        );
    }
}

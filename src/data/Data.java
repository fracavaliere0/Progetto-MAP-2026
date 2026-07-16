package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Modella l'insieme di esempi di training.
 */
public class Data {

    /** Matrice degli esempi di training. */
    private Object[][] data;

    /** Numero di esempi. */
    private int numberOfExamples;

    /** Attributi indipendenti. */
    private Attribute[] explanatorySet;

    /** Attributo di classe. */
    private ContinuousAttribute classAttribute;

    /**
     * Carica un training set da file.
     *
     * @param fileName Nome del file contenente il training set.
     * @throws TrainingDataException se il file non esiste o ha uno schema non valido.
     */
    public Data(String fileName) throws TrainingDataException {
        try (Scanner sc = new Scanner(new File(fileName))) {
            readSchema(sc);
            readData(sc);
        } catch (FileNotFoundException | NumberFormatException e) {
            throw new TrainingDataException(e);
        }
    }

    /**
     * Restituisce il numero di esempi.
     *
     * @return numero di esempi.
     */
    public int getNumberOfExamples() {
        return numberOfExamples;
    }

    /**
     * Restituisce il numero di attributi indipendenti.
     *
     * @return numero di attributi indipendenti.
     */
    public int getNumberOfExplanatoryAttributes() {
        return explanatorySet.length;
    }

    /**
     * Restituisce il valore dell'attributo di classe.
     *
     * @param exampleIndex indice dell'esempio.
     * @return valore dell'attributo di classe.
     */
    public Double getClassValue(int exampleIndex) {
        return (Double) data[exampleIndex][explanatorySet.length];
    }

    /**
     * Restituisce il valore di un attributo indipendente.
     *
     * @param exampleIndex indice dell'esempio.
     * @param attributeIndex indice dell'attributo.
     * @return valore dell'attributo indicato.
     */
    public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
        return data[exampleIndex][attributeIndex];
    }

    /**
     * Restituisce un attributo indipendente.
     *
     * @param index indice dell'attributo.
     * @return attributo indipendente con indice {@code index}.
     */
    public Attribute getExplanatoryAttribute(int index) {
        return explanatorySet[index];
    }

    /**
     * Restituisce l'attributo di classe.
     *
     * @return attributo di classe.
     */
    public ContinuousAttribute getClassAttribute() {
        return classAttribute;
    }

    /**
     * Ordina una porzione del training set rispetto all'attributo indicato.
     *
     * @param attribute attributo da usare per l'ordinamento.
     * @param beginExampleIndex indice iniziale della porzione.
     * @param endExampleIndex indice finale della porzione.
     */
    public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
        int index = attribute.getIndex();
        Arrays.sort(
            data,
            beginExampleIndex,
            endExampleIndex + 1,
            (a, b) -> ((String) a[index]).compareTo((String) b[index])
        );
    }

    /**
     * Restituisce il training set in formato testuale.
     *
     * @return stringa contenente tutti gli esempi.
     */
    @Override
    public String toString() {
        StringBuilder value = new StringBuilder();
        for (Object[] row : data) {
            for (int j = 0; j < explanatorySet.length; j++) {
                value.append(row[j]).append(",");
            }
            value.append(row[explanatorySet.length]).append("\n");
        }
        return value.toString();
    }

    private void readSchema(Scanner sc) throws TrainingDataException {
        if (!sc.hasNextLine()) {
            throw new TrainingDataException("Errore nello schema");
        }

        String line = sc.nextLine();
        if (!line.contains("@schema")) {
            throw new TrainingDataException("Errore nello schema");
        }

        explanatorySet = new Attribute[Integer.parseInt(line.split(" ")[1])];
        int iAttribute = 0;

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] parts = line.split(" ");

            if (parts[0].equals("@data")) {
                numberOfExamples = Integer.parseInt(parts[1]);
                if (classAttribute == null || numberOfExamples <= 0) {
                    throw new TrainingDataException("Errore nello schema");
                }
                return;
            }

            if (parts[0].equals("@desc")) {
                explanatorySet[iAttribute] = new DiscreteAttribute(
                    parts[1],
                    iAttribute,
                    parts[2].split(",")
                );
                iAttribute++;
            } else if (parts[0].equals("@target")) {
                classAttribute = new ContinuousAttribute(parts[1], iAttribute);
            }
        }

        throw new TrainingDataException("Errore nello schema");
    }

    private void readData(Scanner sc) throws TrainingDataException {
        data = new Object[numberOfExamples][explanatorySet.length + 1];
        int iRow = 0;

        while (sc.hasNextLine() && iRow < numberOfExamples) {
            String[] values = sc.nextLine().split(",");
            if (values.length != explanatorySet.length + 1) {
                throw new TrainingDataException("Errore nei dati");
            }
            for (int j = 0; j < explanatorySet.length; j++) {
                data[iRow][j] = values[j].trim();
            }
            data[iRow][explanatorySet.length] = Double.valueOf(values[explanatorySet.length].trim());
            iRow++;
        }

        if (iRow != numberOfExamples) {
            throw new TrainingDataException("Errore nei dati");
        }
    }
}

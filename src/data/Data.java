package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Modella l'insieme di esempi di training.
 */
public class Data {

    /** Matrice degli esempi di training. */
    private Object data[][];

    /** Numero di esempi. */
    private int numberOfExamples;

    /** Attributi indipendenti. */
    private Attribute explanatorySet[];

    /** Attributo di classe. */
    private ContinuousAttribute classAttribute;

    public Data(String fileName) throws TrainingDataException {
        File inFile = new File(fileName);
        Scanner sc = null;

        try {
            sc = new Scanner(inFile);
        } catch (FileNotFoundException e) {
            throw new TrainingDataException(e);
        }

        try {
            if (!sc.hasNextLine()) {
                throw new TrainingDataException("Errore nello schema");
            }

            String line = sc.nextLine();
            if (!line.contains("@schema")) {
                throw new TrainingDataException("Errore nello schema");
            }
            String s[] = line.split(" ");

            explanatorySet = new Attribute[Integer.parseInt(s[1])];
            short iAttribute = 0;
            boolean foundData = false;

            while (sc.hasNextLine() && !foundData) {
                line = sc.nextLine();
                if (line.contains("@data")) {
                    foundData = true;
                } else {
                    s = line.split(" ");
                    if (s[0].equals("@desc")) {
                        String discreteValues[] = s[2].split(",");
                        explanatorySet[iAttribute] = new DiscreteAttribute(
                            s[1],
                            iAttribute,
                            discreteValues
                        );
                        iAttribute++;
                    } else if (s[0].equals("@target")) {
                        classAttribute = new ContinuousAttribute(
                            s[1],
                            iAttribute
                        );
                        iAttribute++;
                    }
                }
            }

            if (!foundData) {
                throw new TrainingDataException("Errore nello schema");
            }

            if (classAttribute == null) {
                throw new TrainingDataException(
                    "Training set privo di variabile target numerica"
                );
            }

            numberOfExamples = Integer.parseInt(line.split(" ")[1]);

            if (numberOfExamples == 0) {
                throw new TrainingDataException("Training set vuoto");
            }

            data = new Object[numberOfExamples][explanatorySet.length + 1];
            short iRow = 0;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                s = line.split(",");
                for (short jColumn = 0; jColumn < s.length - 1; jColumn++) {
                    data[iRow][jColumn] = s[jColumn];
                }
                data[iRow][s.length - 1] = Double.valueOf(s[s.length - 1]);
                iRow++;
            }
        } finally {
            sc.close();
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
     * @param exampleIndex Indice dell'esempio.
     * @return valore dell'attributo di classe.
     */
    public Double getClassValue(int exampleIndex) {
        if (exampleIndex < 0 || exampleIndex >= data.length) {
            throw new IndexOutOfBoundsException(
                "il valore" +
                    exampleIndex +
                    "di exampleIndex all' in getClassValue e' fuori indice"
            );
        }

        return (Double) data[exampleIndex][explanatorySet.length];
    }

    /**
     * Restituisce il valore di un attributo indipendente.
     *
     * @param exampleIndex Indice dell'esempio.
     * @param attributeIndex Indice dell'attributo.
     * @return valore dell'attributo indicato.
     */
    public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
        if (exampleIndex < 0 || exampleIndex >= data.length) {
            throw new IndexOutOfBoundsException(
                "il valore" +
                    exampleIndex +
                    "id exampleIndex in getExplanatoryValue e' fuori indice"
            );
        } else if (
            attributeIndex < 0 || attributeIndex >= explanatorySet.length
        ) {
            throw new IndexOutOfBoundsException(
                "il valore" +
                    attributeIndex +
                    "di attributeIndex all' in getExplanatoryValue e' fuori indice"
            );
        }

        return data[exampleIndex][attributeIndex];
    }

    /**
     * Restituisce un attributo indipendente.
     *
     * @param index Indice dell'attributo.
     * @return attributo indipendente con indice {@code index}.
     */
    public Attribute getExplanatoryAttribute(int index) {
        if (index < 0 || index >= explanatorySet.length) {
            throw new IndexOutOfBoundsException(
                "il valore" +
                    index +
                    "di index in getExplanatoryAttribute e' fuori indice"
            );
        }
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

    public String toString() {
        String value = "";
        for (int i = 0; i < numberOfExamples; i++) {
            for (int j = 0; j < explanatorySet.length; j++) value +=
                data[i][j] + ",";

            value += data[i][explanatorySet.length] + "\n";
        }
        return value;
    }

    public void sort(
        Attribute attribute,
        int beginExampleIndex,
        int endExampleIndex
    ) {
        quicksort(attribute, beginExampleIndex, endExampleIndex);
    }

    // scambio esempio i con esempi oj
    private void swap(int i, int j) {
        Object temp;
        for (int k = 0; k < getNumberOfExplanatoryAttributes() + 1; k++) {
            temp = data[i][k];
            data[i][k] = data[j][k];
            data[j][k] = temp;
        }
    }

    /*
     * Partiziona il vettore rispetto all'elemento x e restiutisce il punto di
     * separazione
     */
    private int partition(DiscreteAttribute attribute, int inf, int sup) {
        int i, j;

        i = inf;
        j = sup;
        int med = (inf + sup) / 2;
        String x = (String) getExplanatoryValue(med, attribute.getIndex());
        swap(inf, med);

        while (true) {
            while (
                i <= sup &&
                (
                    (String) getExplanatoryValue(i, attribute.getIndex())
                ).compareTo(x) <= 0
            ) {
                i++;
            }

            while (
                (
                    (String) getExplanatoryValue(j, attribute.getIndex())
                ).compareTo(x) > 0
            ) {
                j--;
            }

            if (i < j) {
                swap(i, j);
            } else break;
        }
        swap(inf, j);
        return j;
    }

    /*
     * Algoritmo quicksort per l'ordinamento di un array di interi A
     * usando come relazione d'ordine totale "<="
     *
     * @param A
     */
    private void quicksort(Attribute attribute, int inf, int sup) {
        if (sup >= inf) {
            int pos;

            pos = partition((DiscreteAttribute) attribute, inf, sup);

            if ((pos - inf) < (sup - pos + 1)) {
                quicksort(attribute, inf, pos - 1);
                quicksort(attribute, pos + 1, sup);
            } else {
                quicksort(attribute, pos + 1, sup);
                quicksort(attribute, inf, pos - 1);
            }
        }
    }

    public static void main(String[] args) throws TrainingDataException {
        Data trainingSet = new Data("prova.dat");
        System.out.println(trainingSet);

        for (
            int jColumn = 0;
            jColumn < trainingSet.getNumberOfExplanatoryAttributes();
            jColumn++
        ) {
            System.out.println(
                "ORDER BY " + trainingSet.getExplanatoryAttribute(jColumn)
            );
            trainingSet.quicksort(
                trainingSet.getExplanatoryAttribute(jColumn),
                0,
                trainingSet.getNumberOfExamples() - 1
            );
            System.out.println(trainingSet);
        }
    }
}

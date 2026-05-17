package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Modella il training set usato per costruire l'albero di regressione.
 *
 * La classe legge i dati da file, memorizza gli attributi indipendenti,
 * l'attributo di classe e la matrice degli esempi di training. Fornisce inoltre
 * metodi di accesso e ordinamento del sottoinsieme di esempi.
 */
public class Data {

    /** Matrice degli esempi di training organizzata per righe e colonne. */
    private Object data[][];

    /** Numero totale di esempi presenti nel training set. */
    private int numberOfExamples;

    /** Insieme degli attributi indipendenti del dataset. */
    private Attribute explanatorySet[];

    /** Attributo di classe numerico da predire. */
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
                        explanatorySet[iAttribute] = new DiscreteAttribute(s[1], iAttribute, discreteValues);
                        iAttribute++;
                    } else if (s[0].equals("@target")) {
                        classAttribute = new ContinuousAttribute(s[1], iAttribute);
                        iAttribute++;
                    }
                }
            }

            if (!foundData) {
                throw new TrainingDataException("Errore nello schema");
            }

            if (classAttribute == null) {
                throw new TrainingDataException("Training set privo di variabile target numerica");
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
     * Restituisce il numero totale di esempi (righe) presenti nel training set.
     *
     * @return Cardinalità dell'insieme di esempi.
     */
    public int getNumberOfExamples() {
        return numberOfExamples;
    }

    /**
     * Restituisce il numero di attributi indipendenti nel dataset.
     * Corrisponde alla lunghezza dello spazio descrittivo.
     *
     * @return Cardinalità dell'insieme degli attributi indipendenti.
     */
    public int getNumberOfExplanatoryAttributes() {
        return explanatorySet.length;
    }

    /**
     * Restituisce il valore dell'attributo di classe (target) per uno specifico
     * esempio.
     *
     * @param exampleIndex Indice di riga dell'esempio nella matrice dei dati.
     * @return Valore dell'attributo di classe (effettuando il cast a Double).
     * @throws IndexOutOfBoundsException Se {@code exampleIndex} non e' un indice
     *         valido della matrice dei dati.
     */
    public Double getClassValue(int exampleIndex) {
        if (exampleIndex < 0 || exampleIndex >= data.length) {
            throw new IndexOutOfBoundsException(
                    "il valore" + exampleIndex + "di exampleIndex all' in getClassValue e' fuori indice");
        }

        return (Double) data[exampleIndex][explanatorySet.length];
    }

    /**
     * Restituisce l'Object della matrice data incrociando riga e colonna.
     *
     * @param exampleIndex   Indice di riga dell'esempio nella matrice dei dati.
     * @param attributeIndex Indice di colonna dell'attributo indipendente di cui si
     *                       desidera estrarre il valore.
     * @return L'oggetto (valore) associato a quell'attributo per quell'esempio.
     * @throws IndexOutOfBoundsException Se uno degli indici non e' valido.
     */
    public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
        if (exampleIndex < 0 || exampleIndex >= data.length) {
            throw new IndexOutOfBoundsException(
                    "il valore" + exampleIndex + "id exampleIndex in getExplanatoryValue e' fuori indice");
        } else if (attributeIndex < 0 || attributeIndex >= explanatorySet.length) {
            throw new IndexOutOfBoundsException(
                    "il valore" + attributeIndex + "di attributeIndex all' in getExplanatoryValue e' fuori indice");
        }

        return data[exampleIndex][attributeIndex];
    }

    /**
     * Restituisce l'oggetto Attribute trovato in posizione index nell'array
     * explanatorySet.
     *
     * @param index Indice che individua le posizioni nell'array explanatorySet
     * @return L'oggetto (Attribute) presente nell'array nella posizione specificata
      *         da index
     * @throws IndexOutOfBoundsException Se {@code index} non e' un indice valido
     *         dell'array explanatorySet.
     */
    public Attribute getExplanatoryAttribute(int index) {
        if (index < 0 || index >= explanatorySet.length) {
            throw new IndexOutOfBoundsException(
                    "il valore" + index + "di index in getExplanatoryAttribute e' fuori indice");
        }
        return explanatorySet[index];
    }

    /**
     * Restituisce l'attributo target, ovvero la variabile di istanza
     * classAttribute.
     *
     * @return L'oggetto (ContinuousAttribute) che, nella regressione, rappresenta
     *         l'attributo da prevedere.
     */
    public ContinuousAttribute getClassAttribute() {
        return classAttribute;
    }

    public String toString() {
        String value = "";
        for (int i = 0; i < numberOfExamples; i++) {
            for (int j = 0; j < explanatorySet.length; j++)
                value += data[i][j] + ",";

            value += data[i][explanatorySet.length] + "\n";
        }
        return value;

    }

    public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
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

            while (i <= sup && ((String) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
                i++;

            }

            while (((String) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
                j--;

            }

            if (i < j) {
                swap(i, j);
            } else
                break;
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

    public static void main(String args[]) throws TrainingDataException {
        Data trainingSet = new Data("prova.dat");
        System.out.println(trainingSet);

        for (int jColumn = 0; jColumn < trainingSet.getNumberOfExplanatoryAttributes(); jColumn++) {
            System.out.println("ORDER BY " + trainingSet.getExplanatoryAttribute(jColumn));
            trainingSet.quicksort(trainingSet.getExplanatoryAttribute(jColumn), 0,
                    trainingSet.getNumberOfExamples() - 1);
            System.out.println(trainingSet);
        }

    }
}

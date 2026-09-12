package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;



public class Data {

	private Object data [][];
	private int numberOfExamples;
	private List<Attribute> explanatorySet = new LinkedList<Attribute>();
	private ContinuousAttribute classAttribute;

	public Data(String fileName)throws TrainingDataException{
		try (Scanner sc = new Scanner(new File(fileName))) {
			readSchema(sc);
			readData(sc);
		} catch (FileNotFoundException | NumberFormatException e) {
			throw new TrainingDataException(e);
		}
	}

	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	public Double getClassValue(int exampleIndex) {
		return (Double) data[exampleIndex][explanatorySet.size()];
	}

	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data[exampleIndex][attributeIndex];
	}

	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	public ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	public String toString(){
		String value="";
		for(int i=0;i<numberOfExamples;i++){
			for(int j=0;j<explanatorySet.size();j++)
				value+=data[i][j]+",";

			value+=data[i][explanatorySet.size()]+"\n";
		}
		return value;


	}


	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex){

			quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	// scambio esempio i con esempi oj
	private void swap(int i,int j){
		Object temp;
		for (int k=0;k<getNumberOfExplanatoryAttributes()+1;k++){
			temp=data[i][k];
			data[i][k]=data[j][k];
			data[j][k]=temp;
		}

	}




	/*
	 * Partiziona il vettore rispetto all'elemento x e restiutisce il punto di separazione
	 */
	private  int partition(DiscreteAttribute attribute, int inf, int sup){
		int i,j;

		i=inf;
		j=sup;
		int	med=(inf+sup)/2;
		String x=(String)getExplanatoryValue(med, attribute.getIndex());
		swap(inf,med);

		while (true)
		{

			while(i<=sup && ((String)getExplanatoryValue(i, attribute.getIndex())).compareTo(x)<=0){
				i++;

			}

			while(((String)getExplanatoryValue(j, attribute.getIndex())).compareTo(x)>0) {
				j--;

			}

			if(i<j) {
				swap(i,j);
			}
			else break;
		}
		swap(inf,j);
		return j;

	}

	/*
	 * Partiziona il vettore rispetto all'elemento x e restiutisce il punto di separazione
	 */
	private  int partition(ContinuousAttribute attribute, int inf, int sup){
		int i,j;

		i=inf;
		j=sup;
		int	med=(inf+sup)/2;
		Double x=(Double)getExplanatoryValue(med, attribute.getIndex());
		swap(inf,med);

		while (true)
		{

			while(i<=sup && ((Double)getExplanatoryValue(i, attribute.getIndex())).compareTo(x)<=0){
				i++;

			}

			while(((Double)getExplanatoryValue(j, attribute.getIndex())).compareTo(x)>0) {
				j--;

			}

			if(i<j) {
				swap(i,j);
			}
			else break;
		}
		swap(inf,j);
		return j;

	}

	/*
	 * Algoritmo quicksort per l'ordinamento di un array di interi A
	 * usando come relazione d'ordine totale "<="
	 * @param A
	 */
	private void quicksort(Attribute attribute, int inf, int sup){

		if(sup>=inf){

			int pos;
			if(attribute instanceof DiscreteAttribute)
				pos=partition((DiscreteAttribute)attribute, inf, sup);
			else
				pos=partition((ContinuousAttribute)attribute, inf, sup);

			if ((pos-inf) < (sup-pos+1)) {
				quicksort(attribute, inf, pos-1);
				quicksort(attribute, pos+1,sup);
			}
			else
			{
				quicksort(attribute, pos+1, sup);
				quicksort(attribute, inf, pos-1);
			}


		}

	}

	private void readSchema(Scanner sc) throws TrainingDataException {
		if (!sc.hasNextLine())
			throw new TrainingDataException("Errore nello schema");

		String[] schema = sc.nextLine().trim().split("\\s+");
		if (schema.length != 2 || !schema[0].equals("@schema"))
			throw new TrainingDataException("Errore nello schema");

		int numberOfAttributes = Integer.parseInt(schema[1]);
		if (numberOfAttributes < 0)
			throw new TrainingDataException("Errore nello schema");

		explanatorySet = new LinkedList<Attribute>();
		int iAttribute = 0;

		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if (line.isEmpty())
				throw new TrainingDataException("Errore nello schema");

			String[] parts = line.split("\\s+");

			if (parts[0].equals("@data")) {
				if (parts.length != 2 || iAttribute != explanatorySet.size() || classAttribute == null)
					throw new TrainingDataException("Errore nello schema");

				numberOfExamples = Integer.parseInt(parts[1]);
				if (numberOfExamples <= 0)
					throw new TrainingDataException("Errore nello schema");
				return;
			}

			if (parts[0].equals("@desc")) {
				if ((parts.length != 2 && parts.length != 3) || iAttribute >= numberOfAttributes || classAttribute != null)
					throw new TrainingDataException("Errore nello schema");

				if (parts.length == 2) {
					explanatorySet.add(new ContinuousAttribute(parts[1], iAttribute));
				} else {
					String[] discreteValues = parts[2].split(",");
					Set<String> values = new TreeSet<String>(Arrays.asList(discreteValues));
					explanatorySet.add(new DiscreteAttribute(
						parts[1],
						iAttribute,
						values
					));
				}
				iAttribute++;
			} else if (parts[0].equals("@target")) {
				if (parts.length != 2 || iAttribute != numberOfAttributes || classAttribute != null)
					throw new TrainingDataException("Errore nello schema");
				classAttribute = new ContinuousAttribute(parts[1], iAttribute);
			} else {
				throw new TrainingDataException("Errore nello schema");
			}
		}

		throw new TrainingDataException("Errore nello schema");
	}

	private void readData(Scanner sc) throws TrainingDataException {
		data = new Object[numberOfExamples][explanatorySet.size() + 1];
		int iRow = 0;

		while (sc.hasNextLine() && iRow < numberOfExamples) {
			String[] values = sc.nextLine().split(",");
			if (values.length != explanatorySet.size() + 1)
				throw new TrainingDataException("Errore nei dati");

			for (int j = 0; j < explanatorySet.size(); j++) {
				if (explanatorySet.get(j) instanceof DiscreteAttribute)
					data[iRow][j] = values[j].trim();
				else
					data[iRow][j] = Double.valueOf(values[j].trim());
			}
			data[iRow][explanatorySet.size()] = Double.valueOf(values[explanatorySet.size()].trim());
			iRow++;
		}

		if (iRow != numberOfExamples)
			throw new TrainingDataException("Errore nei dati");
	}

	public static void main(String args[])throws TrainingDataException{
		Data trainingSet=new Data("servo.dat");
		System.out.println(trainingSet);



		for(int jColumn=0;jColumn<trainingSet.getNumberOfExplanatoryAttributes();jColumn++)
		{
			System.out.println("ORDER BY "+trainingSet.getExplanatoryAttribute(jColumn));
			trainingSet.quicksort(trainingSet.getExplanatoryAttribute(jColumn),0 , trainingSet.getNumberOfExamples()-1);
			System.out.println(trainingSet);
		}





	}

}

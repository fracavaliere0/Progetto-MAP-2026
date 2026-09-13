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
		Scanner sc = null;
		try {
			File inFile = new File(fileName);
			sc = new Scanner(inFile);
			String line = sc.nextLine();
			if (!line.contains("@schema"))
				throw new TrainingDataException("Errore nello schema");
			String s[] = line.split(" ");
			int numberOfAttributes = new Integer(s[1]);
			if (numberOfAttributes < 0)
				throw new TrainingDataException("Errore nello schema");

			// MAP4: explanatorySet e i domini discreti diventano contenitori.
			short iAttribute = 0;
			line = sc.nextLine();
			while (!line.contains("@data")) {
				s = line.split(" ");
				if (s[0].equals("@desc")) {
					if (classAttribute != null || iAttribute >= numberOfAttributes)
						throw new TrainingDataException("Errore nello schema");
					if (s.length == 2) {
						// MAP5: una descrizione senza dominio indica un attributo continuo.
						explanatorySet.add(new ContinuousAttribute(s[1], iAttribute));
					} else if (s.length == 3) {
						String discreteValues[] = s[2].split(",");
						Set<String> values = new TreeSet<String>(Arrays.asList(discreteValues));
						explanatorySet.add(new DiscreteAttribute(s[1], iAttribute, values));
					} else {
						throw new TrainingDataException("Errore nello schema");
					}
				} else if (s[0].equals("@target")) {
					if (s.length != 2 || classAttribute != null || iAttribute != numberOfAttributes)
						throw new TrainingDataException("Errore nello schema");
					classAttribute = new ContinuousAttribute(s[1], iAttribute);
				} else {
					throw new TrainingDataException("Errore nello schema");
				}
				iAttribute++;
				line = sc.nextLine();
			}
			if (classAttribute == null || explanatorySet.size() != numberOfAttributes)
				throw new TrainingDataException("Errore nello schema");

			numberOfExamples = new Integer(line.split(" ")[1]);
			if (numberOfExamples <= 0)
				throw new TrainingDataException("Training set vuoto");
			data = new Object[numberOfExamples][explanatorySet.size() + 1];
			short iRow = 0;
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				s = line.split(",");
				if (iRow >= numberOfExamples || s.length != explanatorySet.size() + 1)
					throw new TrainingDataException("Errore nei dati");
				for (short jColumn = 0; jColumn < s.length - 1; jColumn++) {
					if (explanatorySet.get(jColumn) instanceof DiscreteAttribute)
						data[iRow][jColumn] = s[jColumn];
					else
						data[iRow][jColumn] = new Double(s[jColumn]);
				}
				data[iRow][s.length - 1] = new Double(s[s.length - 1]);
				iRow++;
			}
			if (iRow != numberOfExamples)
				throw new TrainingDataException("Errore nei dati");
		} catch (FileNotFoundException | RuntimeException e) {
			// MAP3: gli errori di acquisizione diventano TrainingDataException.
			throw new TrainingDataException(e);
		} finally {
			if (sc != null)
				sc.close();
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

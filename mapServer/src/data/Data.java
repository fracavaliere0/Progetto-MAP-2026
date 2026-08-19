package data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import database.Column;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.Example;
import database.TableData;
import database.TableSchema;

public class Data {

	private List<Example> data=new ArrayList<Example>();
	private int numberOfExamples;
	private List<Attribute> explanatorySet = new LinkedList<Attribute>();
	private ContinuousAttribute classAttribute;

	public Data(String tableName) throws TrainingDataException {
		DbAccess db = new DbAccess();
		try {
			db.initConnection();
			TableSchema tableSchema = new TableSchema(db, tableName);
			if (tableSchema.getNumberOfAttributes() < 2) {
				throw new TrainingDataException("La tabella non esiste o contiene meno di due colonne");
			}

			Column classColumn = tableSchema.getColumn(tableSchema.getNumberOfAttributes() - 1);
			if (!classColumn.isNumber()) {
				throw new TrainingDataException("L'ultima colonna della tabella deve essere numerica");
			}

			TableData tableData = new TableData(db);
			data = tableData.getTransazioni(tableName);
			numberOfExamples = data.size();

			for (int i = 0; i < tableSchema.getNumberOfAttributes() - 1; i++) {
				Column column = tableSchema.getColumn(i);
				if (column.isNumber()) {
					explanatorySet.add(new ContinuousAttribute(column.getColumnName(), i));
				} else {
					Set<String> distinctValues = new TreeSet<String>();
					for (Object value : tableData.getDistinctColumnValues(tableName, column)) {
						distinctValues.add((String) value);
					}
					explanatorySet.add(new DiscreteAttribute(column.getColumnName(), i, distinctValues));
				}
			}

			classAttribute = new ContinuousAttribute(classColumn.getColumnName(), explanatorySet.size());
		} catch (DatabaseConnectionException e) {
			throw new TrainingDataException(e);
		} catch (SQLException e) {
			throw new TrainingDataException(e);
		} catch (EmptySetException e) {
			throw new TrainingDataException(e);
		} finally {
			db.closeConnection();
		}
	}

	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	public Double getClassValue(int exampleIndex) {
		if (exampleIndex < 0 || exampleIndex >= data.size()) {
			throw new IndexOutOfBoundsException(
					"il valore" + exampleIndex + "di exampleIndex all' in getClassValue e' fuori indice");
		}

		return (Double) data.get(exampleIndex).get(explanatorySet.size());
	}

	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		if (exampleIndex < 0 || exampleIndex >= data.size()) {
			throw new IndexOutOfBoundsException(
					"il valore" + exampleIndex + "id exampleIndex in getExplanatoryValue e' fuori indice");
		} else if (attributeIndex < 0 || attributeIndex >= explanatorySet.size()) {
			throw new IndexOutOfBoundsException(
					"il valore" + attributeIndex + "di attributeIndex all' in getExplanatoryValue e' fuori indice");
		}

		return data.get(exampleIndex).get(attributeIndex);
	}

	public Attribute getExplanatoryAttribute(int index) {
		if (index < 0 || index >= explanatorySet.size()) {
			throw new IndexOutOfBoundsException(
					"il valore" + index + "di index in getExplanatoryAttribute e' fuori indice");
		}
		return explanatorySet.get(index);
	}

	public ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	public String toString() {
		String value = "";
		for (int i = 0; i < numberOfExamples; i++) {
			for (int j = 0; j < explanatorySet.size(); j++)
				value += data.get(i).get(j) + ",";

			value += data.get(i).get(explanatorySet.size()) + "\n";
		}
		return value;

	}

	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	private void swap(int i, int j) {
		Example temp = data.get(i);
		data.set(i, data.get(j));
		data.set(j, temp);
	}

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

	private int partition(ContinuousAttribute attribute, int inf, int sup) {
		int i, j;

		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		Double x = (Double) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {

			while (i <= sup && ((Double) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;

			}

			while (((Double) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
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

	private void quicksort(Attribute attribute, int inf, int sup) {

		if (sup >= inf) {

			int pos;
			if (attribute instanceof DiscreteAttribute)
				pos = partition((DiscreteAttribute) attribute, inf, sup);
			else
				pos = partition((ContinuousAttribute) attribute, inf, sup);

			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			} else {
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}

		}

	}
}

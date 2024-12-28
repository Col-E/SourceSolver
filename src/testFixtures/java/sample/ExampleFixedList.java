package sample;

import java.util.AbstractList;
import java.util.List;

public class ExampleFixedList<T> extends AbstractList<T> implements List<T> {
	private final Object[] data;

	public ExampleFixedList(int size) {
		data = new Object[size];
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(int index) {
		return (T) data[index];
	}

	@Override
	public int size() {
		return data.length;
	}
}

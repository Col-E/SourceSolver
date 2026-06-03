package sample;

import java.util.List;

public class BoxUseCases {
	Box<String> stringBox = new Box<String>("foo");
	Box<Integer> intBox = new Box<>(100);
	Box<?> wildcardBox = new Box<>("bar");
	Box<? extends Number> numberBox = new Box<Integer>(200);
	List<String> stringList = List.of("foo");
	ExampleFixedList<String> fixedList = new ExampleFixedList<>(1);

	void foo() {
		stringBox.value.toUpperCase();
		intBox.value.intValue();
		wildcardBox.value.hashCode();
		numberBox.value.intValue();
		stringList.get(0).toUpperCase();
		fixedList.get(0).toUpperCase();
	}

	void virtualConsume(Box<?> box) {}

	static void staticConsume(Box<?> box) {}
}

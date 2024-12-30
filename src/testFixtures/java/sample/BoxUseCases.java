package sample;

public class BoxUseCases {
	Box<String> stringBox = new Box<>("foo");
	Box<Integer> intBox = new Box<>(100);

	void foo() {
		stringBox.value.toUpperCase();
		intBox.value.intValue();
	}

	void virtualConsume(Box<?> box) {}

	static void staticConsume(Box<?> box) {}
}

package sample;

import java.util.function.Supplier;

@SuppressWarnings({"all", "unused"})
public class BoxAccess<T extends Number> {
	Box<? super Integer> lowerBox = new Box<Number>(1);

	void use(T value) {
		lowerBox.value.toString();
		provideBox().value.toUpperCase();
		provideBox().toString();
		Box<String> supplied = boxSupplier().get();
		supplied.value.toUpperCase();
		value.intValue();
	}

	Box<String> provideBox() {
		return new Box<>("value");
	}

	Supplier<Box<String>> boxSupplier() {
		return () -> new Box<>("value");
	}
}

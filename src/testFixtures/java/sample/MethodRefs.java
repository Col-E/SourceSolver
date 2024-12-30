package sample;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class MethodRefs {
	IntSupplier bar() {
		return Class.class.getName()::hashCode;
	}

	int foo() {
		BoxUseCases uses = new BoxUseCases();

		Supplier<Box<?>> newBox = Box::new;
		Function<Object, Box<?>> newBoxWithArg = Box::new;
		Consumer<Box<?>> boxConsumerS = BoxUseCases::staticConsume;
		Consumer<Box<?>> boxConsumerV = uses::virtualConsume;


		String text = newBox.get().value instanceof String s ? s : "fallback";
		IntSupplier lengthSupplier = text::length;
		return lengthSupplier.getAsInt();
	}
}

package sample;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class Lambdas {
	static void main() {
		// Two methods of the same name, different lambda parameter
		supply(() -> 0);
		supply(() -> "string");

		// Consumer raw
		Consumer rawConsumer = raw -> raw.notify();
		delegateRaw(Object::notify, new Object());
		delegateRaw(raw -> raw.notify(), new Object());
		delegateRaw(rawConsumer, new Object());

		// Consumer typed
		delegateTyped(String::toLowerCase, "string");
		delegateTyped(str -> str.toLowerCase(), "string");

		// Explicit interface
		File root = new File(".");
		FileParser t = (file, flags) -> {
			return /* lambda */ root.length() + file.length() + flags;
		};
		t = new FileParser() {
			@Override
			public long parse(File fileInner, int flagsInner) {
				return /* inner-class */ root.length() + fileInner.length() + flagsInner;
			}
		};
	}

	static int supply(IntSupplier supplier) {
		return supplier.getAsInt();
	}

	static Object supply(Supplier<?> supplier) {
		return supplier.get();
	}

	static void delegateRaw(BiConsumer raw, Object ob1, Object ob2) {
		raw.accept(ob1, ob2);
	}

	static void delegateRaw(Consumer raw, Object ob) {
		raw.accept(ob);
	}

	static <T> void delegateTyped(Consumer<T> raw, T ob) {
		raw.accept(ob);
	}

	static <T, V> void delegateTyped(BiConsumer<T, V> raw, T ob1, V ob2) {
		raw.accept(ob1, ob2);
	}

	static void noop(Object ob) {}

	static void noopStr(String ob) {}

	interface FileParser {
		long parse(File file, int flags);
	}
}

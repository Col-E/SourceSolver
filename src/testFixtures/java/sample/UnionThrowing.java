package sample;

import java.lang.reflect.Field;

public class UnionThrowing {
	static void foo() {
		try {
			Field field = Class.forName("java.lang.System").getDeclaredField("out");
			field.setAccessible(true);
			field.set(null, null);
		} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
}

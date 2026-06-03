package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoxUseCases {
	Box<String> stringBox = new Box<String>("foo");
	Box<Integer> intBox = new Box<>(100);
	Box<?> wildcardBox = new Box<>("bar");
	Box<? extends Number> numberBox = new Box<Integer>(200);
	List<String> stringList = List.of("foo");
	ExampleFixedList<String> fixedList = new ExampleFixedList<>(1);
	List<List<String>> listOListsOStrings = new ArrayList<>();
	Map<String,Map<String,Map<String,Map<String, String>>>> mapOMapOMapOStrings = new HashMap();

	void foo() {
		stringBox.value.toUpperCase();
		intBox.value.intValue();
		wildcardBox.value.hashCode();
		numberBox.value.intValue();
		stringList.get(0).toUpperCase();
		fixedList.get(0).toUpperCase();
		listOListsOStrings.getFirst().getLast().toUpperCase();
		mapOMapOMapOStrings.get("a").get("b").get("c").get("d").toUpperCase();
		List.copyOf(listOListsOStrings).get(0).get(0).toLowerCase();
	}

	void virtualConsume(Box<?> box) {}

	static void staticConsume(Box<?> box) {}
}

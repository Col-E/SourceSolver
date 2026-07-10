package sample;

import java.util.ArrayList;
import java.util.List;

public class Extractor {
	void localGeneric() {
		List<Item> list = new ArrayList<>();
		list.size();
	}

	void qualifiedGeneric() {
		List<Extractor.Item> list = new ArrayList<>();
		list.size();
	}

	static class Item {}
}

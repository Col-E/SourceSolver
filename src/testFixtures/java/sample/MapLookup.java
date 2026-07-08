package sample;

import java.util.*;

@SuppressWarnings({"all", "unused"})
public class MapLookup {
	private final Map<String, String> headers = Map.of("Accept", "text/plain");

	int countHeaders() {
		return headers.size();
	}
}

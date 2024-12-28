package sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FixedDataProcessor {
	private static final String DATA_SPLIT = "\\D+";
	private static final int PRIMARY_ITEM_SHIFT = 1;

	public static void main(String[] args) {
		if (args.length < 1)
			throw new IllegalArgumentException("Missing argument, path to file");

		Path path = Paths.get(args[0]);
		List<String> lines;
		try {
			lines = Files.readAllLines(path);
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to read input from: " + path, ex);
		}

		ExampleFixedList<String> mappedOutput = mapInput(lines);

		try {
			Files.writeString(path, String.join("\n", mappedOutput));
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to write output to: " + path, ex);
		}
	}

	private static ExampleFixedList<String> mapInput(List<String> lines) {
		ExampleFixedList<String> mappedOutput = new ExampleFixedList<>(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("primary:")) {
				long merged = 0;
				for (String item : line.split(DATA_SPLIT)) {
					merged = (31 * merged) + Long.parseLong(item) << PRIMARY_ITEM_SHIFT;
				}
				mappedOutput.add("primary-merged: " + merged);
			} else if (line.startsWith("alternative:")) {
				long merged = 0;
				for (String item : line.split(DATA_SPLIT)) {
					merged += Long.parseLong(item);
				}
				mappedOutput.add("alternative-merged: " + merged);
			} else if (line.startsWith("skip: ")) {
				i += Integer.parseInt(line.substring(6));
			}
		}
		return mappedOutput;
	}
}

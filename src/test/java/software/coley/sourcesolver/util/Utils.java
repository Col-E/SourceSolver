package software.coley.sourcesolver.util;

import software.coley.collections.Unchecked;
import software.coley.sourcesolver.resolve.entry.BasicEntryPool;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;

import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static software.coley.sourcesolver.resolve.entry.ReflectiveClassEntry.build;

public class Utils {
	private static final EntryPool pool = new BasicEntryPool();
	private static boolean filled;

	public static EntryPool getSharedPool() {
		if (!filled) {
			filled = true;
			fillPool();
		}
		return pool;
	}

	private static void fillPool() {
		List<String> classes = ModuleFinder.ofSystem().findAll().stream()
				.map(modRef -> Unchecked.supply(modRef::open).get()) // open reader to each module
				.flatMap(modReader -> Unchecked.supply(modReader::list).get()) // list all items in the module
				.filter(s -> s.endsWith(".class") && s.indexOf('-') == -1) // retain only classes (except module-info or package-info)
				.map(s -> s.substring(0, s.length() - 6)) // cut off '.class' from the path
				.toList();
		Map<String, ClassEntry> entryMap = new HashMap<>();
		for (String cls : classes) {
			if (cls.indexOf('$') >= 0)
				continue;
			try {
				Class<?> ref = Unchecked.supply(() -> Class.forName(cls.replace('/', '.'), false, ClassLoader.getSystemClassLoader())).get();
				build(entryMap, ref);
			} catch (Throwable ignored) {}
		}

		try {
			Path root = Paths.get("src/testFixtures/java/");
			Files.walk(root, 3).forEach(path -> {
				if (Files.isRegularFile(path)) {
					String pathName = root.relativize(path).toString();
					try {
						String className = pathName.replace(File.separator, ".").replace(".java", "");
						Class<?> cls = Class.forName(className, false, Utils.class.getClassLoader());
						build(entryMap, cls);
					} catch (ReflectiveOperationException ex) {
						throw new IllegalStateException("Failed reflecting test-fixtures, pool not finished populating", ex);
					}
				}
			});
		} catch (IOException ex) {
			throw new IllegalStateException("Failed walking test-fixtures, pool not finished populating", ex);
		}
		for (ClassEntry entry : entryMap.values())
			pool.register(entry);
	}
}

package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicEntryPool implements EntryPool {
	private final Map<String, ClassEntry> classEntries = new HashMap<>();

	@Nonnull
	@Override
	public EntryPool copy() {
		BasicEntryPool copy = new BasicEntryPool();
		copy.classEntries.putAll(classEntries);
		return copy;
	}

	@Override
	public void register(@Nonnull ClassEntry entry) {
		classEntries.put(entry.getName(), entry);
	}

	@Override
	@Nullable
	public ClassEntry getClass(@Nonnull String name) {
		return classEntries.get(name);
	}

	@Nonnull
	@Override
	public List<ClassEntry> getClassesInPackage(@Nullable String packageName) {
		List<ClassEntry> entries = new ArrayList<>();
		if (packageName == null) {
			for (ClassEntry entry : classEntries.values())
				if (entry.getName().indexOf('/') < 0)
					entries.add(entry);
		} else {
			for (ClassEntry entry : classEntries.values())
				if (entry.getName().startsWith(packageName))
					entries.add(entry);
		}
		return entries;
	}
}

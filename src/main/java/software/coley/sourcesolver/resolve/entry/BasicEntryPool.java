package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicEntryPool implements EntryPool {
	private final Map<String, ClassEntry> classEntries = new HashMap<>();

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
			int packageNameLength = packageName.length();
			for (ClassEntry entry : classEntries.values()) {
				// The entry must start with the package name but not include subpackages.
				// We do this by seeing if the entry's last package split character index matches our desired package name length.
				// If so, it is in the same package and not a subpackage.
				String entryName = entry.getName();
				if (entryName.startsWith(packageName) && entryName.lastIndexOf('/') == packageNameLength)
					entries.add(entry);
			}
		}
		return entries;
	}
}

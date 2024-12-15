package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface EntryPool {
	@Nonnull
	EntryPool copy();

	void register(@Nonnull ClassEntry entry);

	@Nullable
	ClassEntry getClass(@Nonnull String name);

	@Nonnull
	List<ClassEntry> getClassesInPackage(@Nullable String packageName);
}

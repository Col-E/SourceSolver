package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Delegating implementation of {@link ClassEntry} which limits member visibility to items that are {@code static}.
 */
public class StaticFilteredClassEntry implements ClassEntry {
	private final ClassEntry delegate;

	public StaticFilteredClassEntry(@Nonnull ClassEntry delegate) {this.delegate = delegate;}

	@Override
	public int getAccess() {
		return delegate.getAccess();
	}

	@Nullable
	@Override
	public ClassEntry getSuperEntry() {
		ClassEntry superEntry = delegate.getSuperEntry();
		if (superEntry == null)
			return null;
		return new StaticFilteredClassEntry(superEntry);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public List<ClassEntry> getImplementedEntries() {
		return (List<ClassEntry>) (List<?>) delegate.getImplementedEntries().stream()
				.map(StaticFilteredClassEntry::new)
				.toList();
	}

	@Nonnull
	@Override
	public String getName() {
		return delegate.getName();
	}

	@Nonnull
	@Override
	public List<FieldEntry> getFields() {
		return delegate.getFields().stream()
				.filter(AccessedEntry::isStatic)
				.collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public List<MethodEntry> getMethods() {
		return delegate.getMethods().stream()
				.filter(AccessedEntry::isStatic)
				.collect(Collectors.toList());
	}
}

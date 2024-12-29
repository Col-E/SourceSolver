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
	public List<ClassEntry> getInnerClassEntries() {
		// We don't record the inner-class attribute details which indicates if the entries are actually static or not.
		// The class model itself won't have the static modifier.
		return delegate.getInnerClassEntries();
	}

	@Nonnull
	@Override
	public String getName() {
		return delegate.getName();
	}

	@Nonnull
	@Override
	public List<FieldEntry> getDeclaredFields() {
		return delegate.getDeclaredFields().stream()
				.filter(AccessedEntry::isStatic)
				.collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public List<MethodEntry> getDeclaredMethods() {
		return delegate.getDeclaredMethods().stream()
				.filter(AccessedEntry::isStatic)
				.collect(Collectors.toList());
	}
}

package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.ClassEntry;

import java.util.List;
import java.util.Objects;

/**
 * Resolution of multiple classes.
 * Used for star imports.
 *
 * @author Matt Coley
 */
non-sealed public interface MultiClassResolution extends Resolution {
	/**
	 * @return List of the resolved classes.
	 */
	@Nonnull
	List<ClassEntry> getClassEntries();

	@Override
	default boolean matches(@Nonnull Resolution other) {
		return other instanceof MultiClassResolution otherMultiClass &&
				Objects.equals(getClassEntries().stream().map(ClassEntry::getName).toList(),
						otherMultiClass.getClassEntries().stream().map(ClassEntry::getName).toList());
	}
}

package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;

import jakarta.annotation.Nonnull;
import java.util.List;

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
}

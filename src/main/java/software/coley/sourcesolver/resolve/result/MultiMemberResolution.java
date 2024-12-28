package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassMemberPair;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Resolution of multiple members.
 * Used for static imports.
 *
 * @author Matt Coley
 */
non-sealed public interface MultiMemberResolution extends Resolution {
	/**
	 * @return List of the resolved members.
	 */
	@Nonnull
	List<ClassMemberPair> getMemberEntries();
}

package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;

import javax.annotation.Nonnull;

/**
 * Resolution of a field or method.
 *
 * @author Matt Coley
 */
sealed public interface MemberResolution extends DescribableResolution permits FieldResolution, MethodResolution {
	/**
	 * @return The resolved class declaring the member.
	 */
	@Nonnull
	ClassEntry getOwnerEntry();

	/**
	 * @return Resolution of the member's declaring class.
	 */
	@Nonnull
	ClassResolution getOwnerResolution();
}

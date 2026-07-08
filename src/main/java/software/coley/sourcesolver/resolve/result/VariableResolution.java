package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

/**
 * Resolution of a local variable or method parameter.
 *
 * @author Matt Coley
 */
non-sealed public interface VariableResolution extends Resolution {
	/**
	 * @return The resolved variable name.
	 */
	@Nonnull
	String getName();

	/**
	 * @return Resolved value type of the variable.
	 */
	@Nonnull
	DescribableEntry getResolvedType();

	@Override
	default boolean matches(@Nonnull Resolution other) {
		return other instanceof VariableResolution otherVariable &&
				getName().equals(otherVariable.getName()) &&
				getResolvedType().getDescriptor().equals(otherVariable.getResolvedType().getDescriptor());
	}
}

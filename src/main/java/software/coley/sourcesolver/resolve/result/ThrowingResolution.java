package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;

/**
 * Resolution of an exception being thrown.
 *
 * @author Matt Coley
 */
non-sealed public interface ThrowingResolution extends Resolution {
	@Override
	default boolean matches(@Nonnull Resolution other) {
		return other instanceof ThrowingResolution;
	}
}

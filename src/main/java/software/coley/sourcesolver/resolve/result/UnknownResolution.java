package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;

/**
 * Resolution failure model.
 *
 * @author Matt Coley
 */
non-sealed public interface UnknownResolution extends Resolution {
	@Override
	default boolean isUnknown() {
		return true;
	}

	@Override
	default boolean matches(@Nonnull Resolution other) {
		return other.isUnknown();
	}
}

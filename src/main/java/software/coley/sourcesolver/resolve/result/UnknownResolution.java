package software.coley.sourcesolver.resolve.result;

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
}

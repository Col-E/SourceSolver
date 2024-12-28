package software.coley.sourcesolver.resolve.entry;

/**
 * Metadata model for a field declaration.
 *
 * @author Matt Coley
 */
public non-sealed interface FieldEntry extends MemberEntry {
	@Override
	default boolean isField() {
		return true;
	}

	@Override
	default boolean isMethod() {
		return false;
	}
}

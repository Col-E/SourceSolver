package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.generic.GenericType;

/**
 * Metadata model for a field declaration.
 *
 * @author Matt Coley
 */
public non-sealed interface FieldEntry extends MemberEntry {
	/**
	 * @return Declared generic type of the field.
	 */
	@Nonnull
	GenericType getGenericType();

	@Override
	default boolean isField() {
		return true;
	}

	@Override
	default boolean isMethod() {
		return false;
	}
}

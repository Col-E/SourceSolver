package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;

/**
 * A model that has an identifier.
 */
public interface NamedModel extends Model {
	/**
	 * @return Identifier name.
	 */
	@Nonnull
	String getName();
}

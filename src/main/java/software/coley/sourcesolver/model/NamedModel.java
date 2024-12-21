package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;

public interface NamedModel extends Model {
	@Nonnull
	String getName();
}

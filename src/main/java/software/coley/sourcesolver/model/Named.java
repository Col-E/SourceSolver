package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Named {
	@Nonnull
	default String getName() {
		NameModel model = getNameModel();
		if (model == null)
			throw new UnsupportedOperationException(getClass().getName() + " must override 'getName'");
		return model.getName();
	}

	/**
	 * @return Model of name.
	 * Can be {@code null} in cases where the {@code javac} API doesn't track names.
	 * The {@link #getName() name string} should still be present though.
	 */
	@Nullable
	NameModel getNameModel();
}

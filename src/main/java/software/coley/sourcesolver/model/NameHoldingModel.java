package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A model that has an identifier as an expression.
 *
 * @author Matt Coley
 */
public interface NameHoldingModel extends NamedModel {
	@Nonnull
	@Override
	default String getName() {
		NameExpressionModel model = getNameModel();
		if (model == null)
			throw new UnsupportedOperationException(getClass().getName() + " must override 'getName' to cover null-model cases");
		return model.getName();
	}

	/**
	 * @return Model of name.
	 * Can be {@code null} in cases where the {@code javac} API doesn't track names.
	 * The {@link #getName() name string} should still be present though.
	 */
	@Nullable
	NameExpressionModel getNameModel();
}

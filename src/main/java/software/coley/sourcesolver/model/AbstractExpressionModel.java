package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class AbstractExpressionModel extends AbstractModel {
	protected AbstractExpressionModel(@Nonnull Range range) {
		super(range);
	}

	protected AbstractExpressionModel(@Nonnull Range range, Model... children) {
		super(range, children);
	}

	protected AbstractExpressionModel(@Nonnull Range range, ChildSupplier... suppliers) {
		super(range, suppliers);
	}

	protected AbstractExpressionModel(@Nonnull Range range, @Nonnull Collection<? extends Model> children) {
		super(range, children);
	}
}

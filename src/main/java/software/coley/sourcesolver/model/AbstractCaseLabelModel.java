package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.Collection;

public abstract class AbstractCaseLabelModel extends AbstractModel {
	protected AbstractCaseLabelModel(@Nonnull Range range) {
		super(range);
	}

	protected AbstractCaseLabelModel(@Nonnull Range range, Model... children) {
		super(range, children);
	}

	protected AbstractCaseLabelModel(@Nonnull Range range, ChildSupplier... suppliers) {
		super(range, suppliers);
	}

	protected AbstractCaseLabelModel(@Nonnull Range range, @Nonnull Collection<? extends Model> children) {
		super(range, children);
	}
}

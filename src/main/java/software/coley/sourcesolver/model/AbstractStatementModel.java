package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class AbstractStatementModel extends AbstractModel {
	protected AbstractStatementModel(@Nonnull Range range) {
		super(range);
	}

	protected AbstractStatementModel(@Nonnull Range range, AbstractModel... children) {
		super(range, children);
	}

	protected AbstractStatementModel(@Nonnull Range range, ChildSupplier... suppliers) {
		super(range, suppliers);
	}

	protected AbstractStatementModel(@Nonnull Range range, @Nonnull Collection<? extends AbstractModel> children) {
		super(range, children);
	}
}

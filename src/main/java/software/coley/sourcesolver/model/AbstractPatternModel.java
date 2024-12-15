package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class AbstractPatternModel extends AbstractModel {
	protected AbstractPatternModel(@Nonnull Range range) {
		super(range);
	}

	protected AbstractPatternModel(@Nonnull Range range, AbstractModel... children) {
		super(range, children);
	}

	protected AbstractPatternModel(@Nonnull Range range, ChildSupplier... suppliers) {
		super(range, suppliers);
	}

	protected AbstractPatternModel(@Nonnull Range range, @Nonnull Collection<? extends AbstractModel> children) {
		super(range, children);
	}
}

package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.Range;

public class ErroneousExpressionStatementModel extends ExpressionStatementModel implements ErroneousModel {
	public ErroneousExpressionStatementModel(@Nonnull Range range, @Nonnull AbstractExpressionModel expression) {
		super(range, expression);
	}
}

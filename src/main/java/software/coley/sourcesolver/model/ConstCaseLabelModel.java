package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class ConstCaseLabelModel extends AbstractCaseLabelModel {
	private final AbstractExpressionModel constExpr;

	public ConstCaseLabelModel(@Nonnull Range range, @Nonnull AbstractExpressionModel constExpr) {
		super(range, constExpr);
		this.constExpr = constExpr;
	}

	@Nonnull
	public AbstractExpressionModel getConstExpr() {
		return constExpr;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConstCaseLabelModel that = (ConstCaseLabelModel) o;

		return constExpr.equals(that.constExpr);
	}

	@Override
	public int hashCode() {
		return constExpr.hashCode();
	}

	@Override
	public String toString() {
		return constExpr.toString();
	}
}

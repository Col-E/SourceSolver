package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class ConditionalExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel condition;
	private final AbstractExpressionModel trueCase;
	private final AbstractExpressionModel falseCase;

	public ConditionalExpressionModel(@Nonnull Range range,
	                                  @Nonnull AbstractExpressionModel condition,
	                                  @Nonnull AbstractExpressionModel trueCase,
	                                  @Nonnull AbstractExpressionModel falseCase) {
		super(range, condition, trueCase, falseCase);
		this.condition = condition;
		this.trueCase = trueCase;
		this.falseCase = falseCase;
	}

	@Nonnull
	public AbstractExpressionModel getCondition() {
		return condition;
	}

	@Nonnull
	public AbstractExpressionModel getTrueCase() {
		return trueCase;
	}

	@Nonnull
	public AbstractExpressionModel getFalseCase() {
		return falseCase;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConditionalExpressionModel that = (ConditionalExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!condition.equals(that.condition)) return false;
		if (!trueCase.equals(that.trueCase)) return false;
		return falseCase.equals(that.falseCase);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + condition.hashCode();
		result = 31 * result + trueCase.hashCode();
		result = 31 * result + falseCase.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return condition + " ? " + trueCase + " : " + falseCase;
	}
}

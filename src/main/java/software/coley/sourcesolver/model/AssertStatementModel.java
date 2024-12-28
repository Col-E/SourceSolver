package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class AssertStatementModel extends AbstractStatementModel {
	private final AbstractExpressionModel condition;
	private final AbstractExpressionModel detail;

	public AssertStatementModel(@Nonnull Range range, @Nonnull AbstractExpressionModel condition, @Nullable AbstractExpressionModel detail) {
		super(range, of(condition), of(detail));
		this.condition = condition;
		this.detail = detail;
	}

	@Nonnull
	public AbstractExpressionModel getCondition() {
		return condition;
	}

	@Nullable
	public AbstractExpressionModel getDetail() {
		return detail;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AssertStatementModel that = (AssertStatementModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!condition.equals(that.condition)) return false;
		return Objects.equals(detail, that.detail);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + condition.hashCode();
		result = 31 * result + (detail != null ? detail.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		if (detail == null)
			return "assert (" + condition + ")";
		return "assert (" + condition + ") : " + detail;
	}
}

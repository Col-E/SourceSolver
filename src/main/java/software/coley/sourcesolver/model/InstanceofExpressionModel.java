package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class InstanceofExpressionModel extends AbstractExpressionModel {
	private final AbstractExpressionModel expression;
	private final AbstractModel type;
	private final AbstractPatternModel pattern;

	public InstanceofExpressionModel(@Nonnull Range range,
	                                 @Nonnull AbstractExpressionModel expression,
	                                 @Nonnull AbstractModel type,
	                                 @Nullable AbstractPatternModel pattern) {
		super(range, of(expression), of(type), of(pattern));
		this.expression = expression;
		this.type = type;
		this.pattern = pattern;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Nonnull
	public AbstractModel getType() {
		return type;
	}

	@Nullable
	public AbstractPatternModel getPattern() {
		return pattern;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InstanceofExpressionModel that = (InstanceofExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!expression.equals(that.expression)) return false;
		if (!type.equals(that.type)) return false;
		return Objects.equals(pattern, that.pattern);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + expression.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		String s = expression + " instanceof " + type;
		if (pattern != null)
			s += " " + pattern;
		return s;
	}
}

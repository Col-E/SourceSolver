package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

/**
 * Model switch pattern matching as described in the JDK 17 preview feature implementation.
 * <br>
 * See: <a href="https://github.com/openjdk/jdk17u/blob/master/src/jdk.compiler/share/classes/com/sun/source/tree/GuardedPatternTree.java">{@code ParenthesizedPatternTree}</a>
 *
 * @author Matt Coley
 */
@SuppressWarnings("unused")
public class GuardedPatternModel extends AbstractPatternModel {
	private final AbstractPatternModel pattern;
	private final AbstractExpressionModel expression;

	public GuardedPatternModel(@Nonnull Range range, @Nonnull AbstractPatternModel pattern, @Nonnull AbstractExpressionModel expression) {
		super(range);

		this.pattern = pattern;
		this.expression = expression;
	}

	@Nonnull
	public AbstractPatternModel getPattern() {
		return pattern;
	}

	@Nonnull
	public AbstractExpressionModel getExpression() {
		return expression;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GuardedPatternModel that = (GuardedPatternModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!pattern.equals(that.pattern)) return false;
		return expression.equals(that.expression);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + pattern.hashCode();
		result = 31 * result + expression.hashCode();
		return result;
	}

	@Override
	public String toString() {
		// Couldn't find an example in the JLS
		return "guard:" + expression + " pattern:" + pattern;
	}
}

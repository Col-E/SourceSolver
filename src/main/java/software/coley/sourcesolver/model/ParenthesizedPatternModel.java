package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public class ParenthesizedPatternModel extends AbstractPatternModel {
	private final AbstractPatternModel pattern;

	public ParenthesizedPatternModel(@Nonnull Range range,
	                                 @Nonnull AbstractPatternModel pattern) {
		super(range, pattern);
		this.pattern = pattern;
	}

	@Nonnull
	public AbstractPatternModel getPattern() {
		return pattern;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ParenthesizedPatternModel that = (ParenthesizedPatternModel) o;

		return pattern.equals(that.pattern) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + pattern.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "(" + pattern + ")";
	}
}

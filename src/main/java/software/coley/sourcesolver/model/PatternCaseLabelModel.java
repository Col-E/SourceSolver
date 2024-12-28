package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class PatternCaseLabelModel extends AbstractCaseLabelModel {
	private final AbstractPatternModel pattern;

	public PatternCaseLabelModel(@Nonnull Range range, @Nonnull AbstractPatternModel pattern) {
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

		PatternCaseLabelModel that = (PatternCaseLabelModel) o;

		return pattern.equals(that.pattern);
	}

	@Override
	public int hashCode() {
		return pattern.hashCode();
	}

	@Override
	public String toString() {
		return pattern.toString();
	}
}

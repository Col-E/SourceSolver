package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class UnknownStatementModel extends AbstractStatementModel {
	private final String content;

	public UnknownStatementModel(@Nonnull Range range, @Nonnull String content) {
		super(range);
		this.content = content;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UnknownStatementModel that = (UnknownStatementModel) o;

		return content.equals(that.content) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		return getRange().hashCode() * 31 + content.hashCode();
	}

	@Override
	public String toString() {
		return content;
	}
}

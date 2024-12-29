package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.Range;

public class ErroneousExpressionModel extends AbstractExpressionModel implements ErroneousModel {
	private final String content;

	public ErroneousExpressionModel(@Nonnull Range range, @Nonnull String content) {
		super(range);
		this.content = content;
	}

	@Nonnull
	public String getContent() {
		return content;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ErroneousExpressionModel that = (ErroneousExpressionModel) o;

		return content.equals(that.content) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + content.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return content;
	}
}

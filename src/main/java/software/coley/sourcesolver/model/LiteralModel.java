package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Objects;

public class LiteralModel extends AbstractModel {
	private final String content;

	public LiteralModel(@Nonnull Range range, @Nonnull String content) {
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
		if (!super.equals(o)) return false;

		LiteralModel that = (LiteralModel) o;

		return Objects.equals(content, that.content);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (content != null ? content.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return content;
	}
}

package software.coley.sourcesolver.model;

import com.sun.source.tree.LiteralTree;
import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;

public class LiteralExpressionModel extends AbstractExpressionModel {
	private final Kind kind;
	private final Object content;

	public LiteralExpressionModel(@Nonnull Range range, @Nonnull Kind kind, @Nullable Object content) {
		super(range);
		this.kind = kind;
		this.content = content;
	}

	@Nonnull
	public Kind getKind() {
		return kind;
	}

	/**
	 * @return {@link LiteralTree#getValue()}
	 */
	@Nullable
	public Object getContent() {
		return content;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiteralExpressionModel that = (LiteralExpressionModel) o;

		if (kind != that.kind) return false;
		return Objects.equals(content, that.content);
	}

	@Override
	public int hashCode() {
		int result = kind.hashCode();
		result = 31 * result + (content != null ? content.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		String contentString = Objects.toString(content);
		if (kind == Kind.STRING)
			return '"' + contentString + '"';
		return contentString;
	}

	public enum Kind {
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		BOOLEAN,
		CHAR,
		STRING,
		NULL,
		VOID,
		ERROR;
	}
}

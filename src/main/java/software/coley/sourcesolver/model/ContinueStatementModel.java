package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ContinueStatementModel extends AbstractStatementModel {
	private final String targetLabel;

	public ContinueStatementModel(@Nonnull Range range, @Nullable String targetLabel) {
		super(range);
		this.targetLabel = targetLabel;
	}

	@Nullable
	public String getTargetLabel() {
		return targetLabel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ContinueStatementModel that = (ContinueStatementModel) o;

		return Objects.equals(targetLabel, that.targetLabel);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (targetLabel != null ? targetLabel.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		if (targetLabel == null)
			return "continue;";
		return "continue " + targetLabel + ";";
	}
}

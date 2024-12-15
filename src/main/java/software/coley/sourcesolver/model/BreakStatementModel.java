package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class BreakStatementModel extends AbstractStatementModel {
	private final String targetLabel;

	public BreakStatementModel(@Nonnull Range range, @Nullable String targetLabel) {
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

		BreakStatementModel that = (BreakStatementModel) o;

		return Objects.equals(targetLabel, that.targetLabel) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		return (targetLabel != null ? targetLabel.hashCode() : 0) + (31 * getRange().hashCode());
	}

	@Override
	public String toString() {
		if (targetLabel == null)
			return "break;";
		return "break " + targetLabel + ";";
	}
}

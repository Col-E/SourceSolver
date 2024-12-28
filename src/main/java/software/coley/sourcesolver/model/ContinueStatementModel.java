package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
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

		ContinueStatementModel that = (ContinueStatementModel) o;

		return Objects.equals(targetLabel, that.targetLabel) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		return (targetLabel != null ? targetLabel.hashCode() : 0);
	}

	@Override
	public String toString() {
		if (targetLabel == null)
			return "continue;";
		return "continue " + targetLabel + ";";
	}
}

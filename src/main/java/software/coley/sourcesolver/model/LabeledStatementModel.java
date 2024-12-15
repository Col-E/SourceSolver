package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class LabeledStatementModel extends AbstractStatementModel {
	private final String labelName;
	private final AbstractStatementModel statement;

	public LabeledStatementModel(@Nonnull Range range, @Nullable String labelName, @Nonnull AbstractStatementModel statement) {
		super(range);
		this.labelName = labelName;
		this.statement = statement;
	}

	@Nullable
	public String getLabelName() {
		return labelName;
	}

	@Nonnull
	public AbstractStatementModel getStatement() {
		return statement;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LabeledStatementModel that = (LabeledStatementModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!Objects.equals(labelName, that.labelName)) return false;
		return statement.equals(that.statement);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + (labelName != null ? labelName.hashCode() : 0);
		result = 31 * result + statement.hashCode();
		return result;
	}

	@Override
	public String toString() {
		if (labelName == null)
			return "<nameless> { " + statement + " }";
		return labelName + " { " + statement + " }";
	}
}

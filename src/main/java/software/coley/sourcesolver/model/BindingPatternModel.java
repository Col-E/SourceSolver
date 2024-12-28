package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;

public class BindingPatternModel extends AbstractPatternModel {
	private final VariableModel variable;

	public BindingPatternModel(@Nonnull Range range,
	                           @Nonnull VariableModel variable) {
		super(range, variable);
		this.variable = variable;
	}

	@Nonnull
	public VariableModel getVariable() {
		return variable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BindingPatternModel that = (BindingPatternModel) o;

		return variable.equals(that.variable) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		return variable.hashCode() + 31 * getRange().hashCode();
	}

	@Override
	public String toString() {
		return variable.toString();
	}
}

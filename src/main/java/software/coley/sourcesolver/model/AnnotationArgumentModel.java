package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class AnnotationArgumentModel extends AbstractModel implements NameHolder {
	private final NameExpressionModel nameModel;
	private final AbstractExpressionModel valueModel;

	public AnnotationArgumentModel(@Nonnull Range range,
	                               @Nullable NameExpressionModel nameModel,
	                               @Nonnull AbstractExpressionModel valueModel) {
		super(range, of(nameModel), of(valueModel));
		this.nameModel = nameModel;
		this.valueModel = valueModel;
	}

	@Nonnull
	@Override
	public String getName() {
		// Edge case for implicit named argument
		if (nameModel == null)
			return "value";
		return nameModel.getName();
	}

	@Nullable
	@Override
	public NameExpressionModel getNameModel() {
		return nameModel;
	}

	@Nonnull
	public AbstractExpressionModel getValueModel() {
		return valueModel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AnnotationArgumentModel that = (AnnotationArgumentModel) o;

		if (!nameModel.equals(that.nameModel)) return false;
		if (!getRange().equals(that.getRange())) return false;
		return valueModel.equals(that.valueModel);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + nameModel.hashCode();
		result = 31 * result + valueModel.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getName() + " = " + valueModel.toString();
	}
}

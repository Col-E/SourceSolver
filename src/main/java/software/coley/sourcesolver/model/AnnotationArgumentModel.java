package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnnotationArgumentModel extends AbstractModel implements Named {
	private final NameModel nameModel;
	private final AbstractModel valueModel;

	public AnnotationArgumentModel(@Nonnull Range range,
	                               @Nullable NameModel nameModel,
	                               @Nonnull AbstractModel valueModel) {
		super(range, nameModel, valueModel);
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
	public NameModel getNameModel() {
		return nameModel;
	}

	@Nonnull
	public AbstractModel getValueModel() {
		return valueModel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		AnnotationArgumentModel that = (AnnotationArgumentModel) o;

		if (!nameModel.equals(that.nameModel)) return false;
		return valueModel.equals(that.valueModel);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + nameModel.hashCode();
		result = 31 * result + valueModel.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getName() + " = " + valueModel.toString();
	}
}

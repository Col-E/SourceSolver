package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationExpressionModel extends AbstractExpressionModel implements Named {
	private final NameExpressionModel nameModel;
	private final List<AnnotationArgumentModel> argumentModels;

	public AnnotationExpressionModel(@Nonnull Range range, @Nonnull NameExpressionModel nameModel,
	                                 @Nonnull List<AnnotationArgumentModel> argumentModels) {
		super(range);
		this.nameModel = nameModel;
		this.argumentModels = argumentModels;
	}

	@Nonnull
	@Override
	public NameExpressionModel getNameModel() {
		return nameModel;
	}

	@Nonnull
	public List<AnnotationArgumentModel> getArgumentModels() {
		return argumentModels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		AnnotationExpressionModel that = (AnnotationExpressionModel) o;

		if (!nameModel.equals(that.nameModel)) return false;
		return argumentModels.equals(that.argumentModels);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + nameModel.hashCode();
		result = 31 * result + argumentModels.hashCode();
		return result;
	}

	@Override
	public String toString() {
		String display = "@" + nameModel;
		if (!argumentModels.isEmpty()) {
			display += "(" + argumentModels.stream()
					.map(AnnotationArgumentModel::toString)
					.collect(Collectors.joining(", ")) +
					")";

		}
		return display;
	}
}

package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnnotationUseModel extends AbstractModel implements Named {
	private final NameModel nameModel;
	private final List<AnnotationArgumentModel> argumentModels;

	public AnnotationUseModel(@Nonnull Range range, @Nonnull NameModel nameModel,
	                          @Nonnull List<AnnotationArgumentModel> argumentModels) {
		super(range);
		this.nameModel = nameModel;
		this.argumentModels = argumentModels;
	}

	@Nonnull
	@Override
	public NameModel getNameModel() {
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

		AnnotationUseModel that = (AnnotationUseModel) o;

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

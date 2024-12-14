package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ImplementsModel extends AbstractModel {
	public static final ImplementsModel EMPTY = new ImplementsModel(Range.UNKNOWN, Collections.emptyList());
	private final List<NameExpressionModel> implementedClassNameModels;

	public ImplementsModel(@Nonnull Range range, @Nonnull List<NameExpressionModel> implementedClassNameModels) {
		super(range);
		this.implementedClassNameModels = Collections.unmodifiableList(implementedClassNameModels);
	}

	@Nonnull
	public List<NameExpressionModel> getImplementedClassNameModels() {
		return implementedClassNameModels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ImplementsModel that = (ImplementsModel) o;

		return implementedClassNameModels.equals(that.implementedClassNameModels);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + implementedClassNameModels.hashCode();
		return result;
	}

	@Override
	public String toString() {
		if (implementedClassNameModels.isEmpty())
			return "";
		return "implements " + implementedClassNameModels.stream()
				.map(NameExpressionModel::getName)
				.collect(Collectors.joining(", "));
	}
}

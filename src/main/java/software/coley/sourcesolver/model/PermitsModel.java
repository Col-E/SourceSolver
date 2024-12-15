package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PermitsModel extends AbstractModel {
	public static final PermitsModel EMPTY = new PermitsModel(Range.UNKNOWN, Collections.emptyList());
	private final List<NameExpressionModel> permittedClassNameModels;

	public PermitsModel(@Nonnull Range range, @Nonnull List<NameExpressionModel> permittedClassNameModels) {
		super(range);
		this.permittedClassNameModels = Collections.unmodifiableList(permittedClassNameModels);
	}

	@Nonnull
	public List<NameExpressionModel> getPermittedClassNameModels() {
		return permittedClassNameModels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PermitsModel that = (PermitsModel) o;

		return permittedClassNameModels.equals(that.permittedClassNameModels) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + permittedClassNameModels.hashCode();
		return result;
	}

	@Override
	public String toString() {
		if (permittedClassNameModels.isEmpty())
			return "";
		return "permits " + permittedClassNameModels.stream()
				.map(NameExpressionModel::getName)
				.collect(Collectors.joining(", "));
	}
}

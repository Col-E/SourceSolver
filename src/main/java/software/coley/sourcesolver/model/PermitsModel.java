package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PermitsModel extends AbstractModel {
	public static final PermitsModel EMPTY = new PermitsModel(Range.UNKNOWN, Collections.emptyList());
	private final List<NameModel> permittedClassNameModels;

	public PermitsModel(@Nonnull Range range, @Nonnull List<NameModel> permittedClassNameModels) {
		super(range);
		this.permittedClassNameModels = Collections.unmodifiableList(permittedClassNameModels);
	}

	@Nonnull
	public List<NameModel> getPermittedClassNameModels() {
		return permittedClassNameModels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		PermitsModel that = (PermitsModel) o;

		return permittedClassNameModels.equals(that.permittedClassNameModels);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + permittedClassNameModels.hashCode();
		return result;
	}

	@Override
	public String toString() {
		if (permittedClassNameModels.isEmpty())
			return "";
		return "permits " + permittedClassNameModels.stream()
				.map(NameModel::getName)
				.collect(Collectors.joining(", "));
	}
}

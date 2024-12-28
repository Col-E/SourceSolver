package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PermitsModel extends AbstractModel {
	public static final PermitsModel EMPTY = new PermitsModel(Range.UNKNOWN, Collections.emptyList());
	private final List<NamedModel> permittedClassNames;

	public PermitsModel(@Nonnull Range range, @Nonnull List<NamedModel> permittedClassNames) {
		super(range);
		this.permittedClassNames = Collections.unmodifiableList(permittedClassNames);
	}

	@Nonnull
	public List<NamedModel> getPermittedClassNames() {
		return permittedClassNames;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PermitsModel that = (PermitsModel) o;

		return permittedClassNames.equals(that.permittedClassNames) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + permittedClassNames.hashCode();
		return result;
	}

	@Override
	public String toString() {
		if (permittedClassNames.isEmpty())
			return "";
		return "permits " + permittedClassNames.stream()
				.map(NamedModel::getName)
				.collect(Collectors.joining(", "));
	}
}

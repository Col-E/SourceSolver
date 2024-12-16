package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class ImplementsModel extends AbstractModel {
	public static final ImplementsModel EMPTY = new ImplementsModel(Range.UNKNOWN, Collections.emptyList());
	private final List<NameExpressionModel> implementedClassNames;

	public ImplementsModel(@Nonnull Range range, @Nonnull List<NameExpressionModel> implementedClassNames) {
		super(range, of(implementedClassNames));
		this.implementedClassNames = Collections.unmodifiableList(implementedClassNames);
	}

	@Nonnull
	public List<NameExpressionModel> getImplementedClassNames() {
		return implementedClassNames;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ImplementsModel that = (ImplementsModel) o;

		return implementedClassNames.equals(that.implementedClassNames) && getRange().equals(that.getRange());
	}

	@Override
	public int hashCode() {
		return implementedClassNames.hashCode() + (31 * getRange().hashCode());
	}

	@Override
	public String toString() {
		if (implementedClassNames.isEmpty())
			return "";
		return "implements " + implementedClassNames.stream()
				.map(NameExpressionModel::getName)
				.collect(Collectors.joining(", "));
	}
}

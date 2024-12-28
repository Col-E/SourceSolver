package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class PackageModel extends AbstractModel implements AnnotatedModel, NameHoldingModel {
	public static final PackageModel DEFAULT_PACKAGE = new PackageModel(Range.UNKNOWN, new NameExpressionModel(Range.UNKNOWN, ""), Collections.emptyList());
	private final NameExpressionModel name;
	private final List<AnnotationExpressionModel> annotations;

	public PackageModel(@Nonnull Range range, @Nonnull NameExpressionModel name, @Nonnull List<AnnotationExpressionModel> annotations) {
		super(range, of(name), of(annotations));
		this.name = name;
		this.annotations = annotations;
	}

	public boolean isDefaultPackage() {
		return name.getName().isEmpty() && annotations.isEmpty();
	}

	@Nonnull
	@Override
	public List<AnnotationExpressionModel> getAnnotations() {
		return annotations;
	}

	@Nonnull
	@Override
	public NameExpressionModel getNameModel() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PackageModel that = (PackageModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!name.equals(that.name)) return false;
		return annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + annotations.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "package " + name.getName();
	}
}

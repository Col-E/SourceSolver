package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class TypeParameterModel extends AbstractModel implements AnnotatedModel {
	private final String name;
	private final List<Model> bounds;
	private final List<AnnotationExpressionModel> annotations;

	public TypeParameterModel(@Nonnull Range range, @Nonnull String name,
	                          @Nonnull List<Model> bounds, @Nonnull List<AnnotationExpressionModel> annotations) {
		super(range, of(bounds), of(annotations));
		this.name = name;
		this.bounds = bounds;
		this.annotations = annotations;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nonnull
	public List<Model> getBounds() {
		return bounds;
	}

	@Nonnull
	@Override
	public List<AnnotationExpressionModel> getAnnotations() {
		return annotations;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TypeParameterModel that = (TypeParameterModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!name.equals(that.name)) return false;
		if (!bounds.equals(that.bounds)) return false;
		return annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + bounds.hashCode();
		result = 31 * result + annotations.hashCode();
		return result;
	}

	@Override
	public String toString() {
		if (bounds.isEmpty())
			return name;
		return name + " extends " + bounds.stream().map(Object::toString).collect(Collectors.joining(", "));
	}
}

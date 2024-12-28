package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class ArrayDeclarationExpressionModel extends AbstractExpressionModel implements AnnotatedModel {
	private final TypeModel type;
	private final List<AbstractExpressionModel> dimensions;
	private final List<AbstractExpressionModel> initializers;
	private final List<AnnotationExpressionModel> annotations;

	public ArrayDeclarationExpressionModel(@Nonnull Range range,
	                                       @Nonnull TypeModel type,
	                                       @Nonnull List<AbstractExpressionModel> dimensions,
	                                       @Nonnull List<AbstractExpressionModel> initializers,
	                                       @Nonnull List<AnnotationExpressionModel> annotations) {
		super(range, of(type), of(dimensions), of(initializers), of(annotations));
		this.type = type;
		this.dimensions = dimensions;
		this.initializers = initializers;
		this.annotations = annotations;
	}

	@Nonnull
	public TypeModel getType() {
		return type;
	}

	@Nonnull
	public List<AbstractExpressionModel> getDimensions() {
		return dimensions;
	}

	@Nonnull
	public List<AbstractExpressionModel> getInitializers() {
		return initializers;
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

		ArrayDeclarationExpressionModel that = (ArrayDeclarationExpressionModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!type.equals(that.type)) return false;
		if (!dimensions.equals(that.dimensions)) return false;
		if (!initializers.equals(that.initializers)) return false;
		return annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + dimensions.hashCode();
		result = 31 * result + initializers.hashCode();
		result = 31 * result + annotations.hashCode();
		return result;
	}

	@Override
	public String toString() {
		// TODO: Flesh out properly
		//     new type dimensions initializers
		//     new type dimensions [ ] initializers
		return "new " + type + "[]".repeat(dimensions.size());
	}
}

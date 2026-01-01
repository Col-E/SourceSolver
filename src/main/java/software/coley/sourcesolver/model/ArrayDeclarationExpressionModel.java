package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.util.Range;

import java.util.List;
import java.util.stream.Collectors;

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

	/**
	 * Present when the array is declared with dimensions.
	 * <pre>{@code int[] foo = new int[100]; // Single dimension 100}</pre>
	 * <pre>{@code int[][] foo = new int[100][200]; // Two dimensions 100 and 200}</pre>
	 * Not present if {@link #getInitializers() initializers} are specified.
	 *
	 * @return List of dimension expressions.
	 */
	@Nonnull
	public List<AbstractExpressionModel> getDimensions() {
		return dimensions;
	}

	/**
	 * Present when the array is declared with an initializer.
	 * <pre>{@code int[] foo = {1, 2, 3}; // Three initializer expressions}</pre>
	 * <pre>{@code int[][] foo = {new int[1], new int[1], new int[1]}; // Still three initializer expressions}</pre>
	 * Not present if {@link #getDimensions() dimensions} are specified.
	 *
	 * @return List of initializer expressions.
	 */
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
		if (!dimensions.isEmpty())
			return "new " + type + "[" + dimensions.stream().map(Object::toString).collect(Collectors.joining("][")) + "]";
		return "new " + type + "{ " + initializers.stream().map(Object::toString).collect(Collectors.joining(", ")) + " }";
	}
}

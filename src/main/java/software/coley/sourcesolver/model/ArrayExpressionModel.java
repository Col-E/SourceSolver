package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class ArrayExpressionModel extends AbstractExpressionModel implements Annotated {
	private final List<AbstractExpressionModel> dimensionModels;
	private final List<AbstractExpressionModel> initializersModels;
	private final List<AnnotationExpressionModel> annotationModels;

	public ArrayExpressionModel(@Nonnull Range range,
	                            @Nonnull List<AbstractExpressionModel> dimensionModels,
	                            @Nonnull List<AbstractExpressionModel> initializersModels,
	                            @Nonnull List<AnnotationExpressionModel> annotationModels) {
		super(range, of(dimensionModels), of(initializersModels), of(annotationModels));
		this.dimensionModels = dimensionModels;
		this.initializersModels = initializersModels;
		this.annotationModels = annotationModels;
	}

	@Nonnull
	public List<AbstractExpressionModel> getDimensionModels() {
		return dimensionModels;
	}

	@Nonnull
	public List<AbstractExpressionModel> getInitializersModels() {
		return initializersModels;
	}

	@Nonnull
	@Override
	public List<AnnotationExpressionModel> getAnnotationModels() {
		return annotationModels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ArrayExpressionModel that = (ArrayExpressionModel) o;

		if (!dimensionModels.equals(that.dimensionModels)) return false;
		if (!initializersModels.equals(that.initializersModels)) return false;
		return annotationModels.equals(that.annotationModels);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + dimensionModels.hashCode();
		result = 31 * result + initializersModels.hashCode();
		result = 31 * result + annotationModels.hashCode();
		return result;
	}
}

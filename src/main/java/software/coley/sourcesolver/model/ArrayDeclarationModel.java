package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class ArrayDeclarationModel extends AbstractModel implements Annotated {
	private final List<AbstractModel> dimensionModels;
	private final List<AbstractModel> initializersModels;
	private final List<AnnotationUseModel> annotationModels;

	public ArrayDeclarationModel(@Nonnull Range range,
	                             @Nonnull List<AbstractModel> dimensionModels,
	                             @Nonnull List<AbstractModel> initializersModels,
	                             @Nonnull List<AnnotationUseModel> annotationModels) {
		super(range, of(dimensionModels), of(initializersModels), of(annotationModels));
		this.dimensionModels = dimensionModels;
		this.initializersModels = initializersModels;
		this.annotationModels = annotationModels;
	}

	@Nonnull
	public List<AbstractModel> getDimensionModels() {
		return dimensionModels;
	}

	@Nonnull
	public List<AbstractModel> getInitializersModels() {
		return initializersModels;
	}

	@Nonnull
	@Override
	public List<AnnotationUseModel> getAnnotationModels() {
		return annotationModels;
	}
}

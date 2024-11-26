package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class PackageModel extends AbstractModel implements Annotated, Named {
	public static final PackageModel DEFAULT_PACKAGE = new PackageModel(Range.UNKNOWN, new NameModel(Range.UNKNOWN, ""), Collections.emptyList());
	private final NameModel nameModel;
	private final List<AnnotationUseModel> annotationModels;

	public PackageModel(@Nonnull Range range, @Nonnull NameModel nameModel, @Nonnull List<AnnotationUseModel> annotationModels) {
		super(range, of(nameModel), of(annotationModels));
		this.nameModel = nameModel;
		this.annotationModels = annotationModels;
	}

	@Override
	public String toString() {
		return "package " + nameModel.getName();
	}

	@Nonnull
	@Override
	public List<AnnotationUseModel> getAnnotationModels() {
		return annotationModels;
	}

	@Nonnull
	@Override
	public NameModel getNameModel() {
		return nameModel;
	}
}

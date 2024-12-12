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

	public boolean isDefaultPackage() {
		return nameModel.getName().isEmpty() && annotationModels.isEmpty();
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		PackageModel that = (PackageModel) o;

		if (!nameModel.equals(that.nameModel)) return false;
		return annotationModels.equals(that.annotationModels);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + nameModel.hashCode();
		result = 31 * result + annotationModels.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "package " + nameModel.getName();
	}
}

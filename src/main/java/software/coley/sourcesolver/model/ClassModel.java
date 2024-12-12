package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class ClassModel extends AbstractModel implements Annotated, Named {
	private final PackageModel packageModel;
	private final List<ImportModel> importModels;
	private final List<AnnotationUseModel> annotationModels;
	private final ModifiersModel modifiersModel;
	private final String name;
	private final List<TypeParameterModel> typeParameterModels;
	private final NameModel extendsModel;
	private final ImplementsModel implementsModel;
	private final PermitsModel permitsModel;
	private final List<VariableModel> fieldModels;
	private final List<MethodModel> methodModels;
	private final List<ClassModel> innerClassModels;

	public ClassModel(@Nonnull Range range,
	                  @Nonnull PackageModel packageModel,
	                  @Nonnull List<ImportModel> importModels,
	                  @Nonnull List<AnnotationUseModel> annotationModels,
	                  @Nonnull ModifiersModel modifiersModel,
	                  @Nonnull String name,
	                  @Nonnull List<TypeParameterModel> typeParameterModels,
	                  @Nonnull NameModel extendsModel,
	                  @Nonnull ImplementsModel implementsModel,
	                  @Nonnull PermitsModel permitsModel,
	                  @Nonnull List<VariableModel> fieldModels,
	                  @Nonnull List<MethodModel> methodModels,
	                  @Nonnull List<ClassModel> innerClassModels) {
		super(range, of(packageModel), of(importModels), of(annotationModels), of(fieldModels), of(methodModels), of(innerClassModels));
		this.packageModel = packageModel;
		this.importModels = Collections.unmodifiableList(importModels);
		this.annotationModels = Collections.unmodifiableList(annotationModels);
		this.modifiersModel = modifiersModel;
		this.name = name;
		this.typeParameterModels = typeParameterModels;
		this.extendsModel = extendsModel;
		this.implementsModel = implementsModel;
		this.permitsModel = permitsModel;
		this.fieldModels = Collections.unmodifiableList(fieldModels);
		this.methodModels = Collections.unmodifiableList(methodModels);
		this.innerClassModels = Collections.unmodifiableList(innerClassModels);
	}

	@Nonnull
	public PackageModel getPackageModel() {
		return packageModel;
	}

	@Nonnull
	public List<ImportModel> getImportModels() {
		return importModels;
	}

	@Nonnull
	@Override
	public List<AnnotationUseModel> getAnnotationModels() {
		return annotationModels;
	}

	@Nonnull
	public ModifiersModel getModifiersModel() {
		return modifiersModel;
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nonnull
	public List<TypeParameterModel> getTypeParameterModels() {
		return typeParameterModels;
	}

	@Nonnull
	public NameModel getExtendsModel() {
		return extendsModel;
	}

	@Nonnull
	public ImplementsModel getImplementsModel() {
		return implementsModel;
	}

	@Nonnull
	public PermitsModel getPermitsModel() {
		return permitsModel;
	}

	@Nullable
	@Override
	public NameModel getNameModel() {
		// Javac doesn't have an AST covering the class's name
		return null;
	}

	@Nonnull
	public List<VariableModel> getFieldModels() {
		return fieldModels;
	}

	@Nonnull
	public List<MethodModel> getMethodModels() {
		return methodModels;
	}

	@Nonnull
	public List<ClassModel> getInnerClassModels() {
		return innerClassModels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ClassModel that = (ClassModel) o;

		if (!packageModel.equals(that.packageModel)) return false;
		if (!importModels.equals(that.importModels)) return false;
		if (!annotationModels.equals(that.annotationModels)) return false;
		if (!modifiersModel.equals(that.modifiersModel)) return false;
		if (!name.equals(that.name)) return false;
		if (!typeParameterModels.equals(that.typeParameterModels)) return false;
		if (!extendsModel.equals(that.extendsModel)) return false;
		if (!implementsModel.equals(that.implementsModel)) return false;
		if (!permitsModel.equals(that.permitsModel)) return false;
		if (!fieldModels.equals(that.fieldModels)) return false;
		if (!methodModels.equals(that.methodModels)) return false;
		return innerClassModels.equals(that.innerClassModels);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + packageModel.hashCode();
		result = 31 * result + importModels.hashCode();
		result = 31 * result + annotationModels.hashCode();
		result = 31 * result + modifiersModel.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + typeParameterModels.hashCode();
		result = 31 * result + extendsModel.hashCode();
		result = 31 * result + implementsModel.hashCode();
		result = 31 * result + permitsModel.hashCode();
		result = 31 * result + fieldModels.hashCode();
		result = 31 * result + methodModels.hashCode();
		result = 31 * result + innerClassModels.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (packageModel.isDefaultPackage()) {

		} else {
			sb.append(packageModel.getName()).append('.');
		}
		sb.append(name);

		if (fieldModels.isEmpty() && methodModels.isEmpty()) {
			sb.append(" {}");
		} else {
			sb.append(" {\n    // Fields");
			for (VariableModel fieldModel : fieldModels)
				sb.append("\n    ").append(fieldModel.toString());
			sb.append("\n    // Methods");
			for (MethodModel methodModel : methodModels)
				sb.append("\n    ").append(methodModel.toString());
			sb.append("\n}");
		}
		return sb.toString();
	}
}

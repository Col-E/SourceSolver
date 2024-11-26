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

	@Override
	public String toString() {
		return name;
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
}

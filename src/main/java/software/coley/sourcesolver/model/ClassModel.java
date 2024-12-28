package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class ClassModel extends AbstractStatementModel implements AnnotatedModel, NameHoldingModel {
	private final List<AnnotationExpressionModel> annotations;
	private final ModifiersModel modifiers;
	private final String name;
	private final List<TypeParameterModel> typeParameters;
	private final NamedModel extendsModel;
	private final ImplementsModel implementsModel;
	private final PermitsModel permitsModel;
	private final List<VariableModel> fields;
	private final List<MethodModel> methods;
	private final List<ClassModel> innerClasses;

	public ClassModel(@Nonnull Range range,
	                  @Nonnull List<AnnotationExpressionModel> annotations,
	                  @Nonnull ModifiersModel modifiers,
	                  @Nonnull String name,
	                  @Nonnull List<TypeParameterModel> typeParameters,
	                  @Nonnull NamedModel extendsModel,
	                  @Nonnull ImplementsModel implementsModel,
	                  @Nonnull PermitsModel permitsModel,
	                  @Nonnull List<VariableModel> fields,
	                  @Nonnull List<MethodModel> methods,
	                  @Nonnull List<ClassModel> innerClasses) {
		super(range, of(annotations), of(typeParameters), of(extendsModel), of(implementsModel),
				of(permitsModel), of(fields), of(methods), of(innerClasses));
		this.annotations = Collections.unmodifiableList(annotations);
		this.modifiers = modifiers;
		this.name = name;
		this.typeParameters = typeParameters;
		this.extendsModel = extendsModel;
		this.implementsModel = implementsModel;
		this.permitsModel = permitsModel;
		this.fields = Collections.unmodifiableList(fields);
		this.methods = Collections.unmodifiableList(methods);
		this.innerClasses = Collections.unmodifiableList(innerClasses);
	}

	@Nonnull
	@Override
	public List<AnnotationExpressionModel> getAnnotations() {
		return annotations;
	}

	@Nonnull
	public ModifiersModel getModifiers() {
		return modifiers;
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nonnull
	public List<TypeParameterModel> getTypeParameters() {
		return typeParameters;
	}

	@Nonnull
	public NamedModel getExtends() {
		return extendsModel;
	}

	@Nonnull
	public ImplementsModel getImplements() {
		return implementsModel;
	}

	@Nonnull
	public PermitsModel getPermits() {
		return permitsModel;
	}

	@Nullable
	@Override
	public NameExpressionModel getNameModel() {
		// Javac doesn't have an AST covering the class's name
		return null;
	}

	@Nonnull
	public List<VariableModel> getFields() {
		return fields;
	}

	@Nonnull
	public List<MethodModel> getMethods() {
		return methods;
	}

	@Nonnull
	public List<ClassModel> getInnerClasses() {
		return innerClasses;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClassModel that = (ClassModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!annotations.equals(that.annotations)) return false;
		if (!modifiers.equals(that.modifiers)) return false;
		if (!name.equals(that.name)) return false;
		if (!typeParameters.equals(that.typeParameters)) return false;
		if (!extendsModel.equals(that.extendsModel)) return false;
		if (!implementsModel.equals(that.implementsModel)) return false;
		if (!permitsModel.equals(that.permitsModel)) return false;
		if (!fields.equals(that.fields)) return false;
		if (!methods.equals(that.methods)) return false;
		return innerClasses.equals(that.innerClasses);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + annotations.hashCode();
		result = 31 * result + modifiers.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + typeParameters.hashCode();
		result = 31 * result + extendsModel.hashCode();
		result = 31 * result + implementsModel.hashCode();
		result = 31 * result + permitsModel.hashCode();
		result = 31 * result + fields.hashCode();
		result = 31 * result + methods.hashCode();
		result = 31 * result + innerClasses.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("class ").append(name);

		if (fields.isEmpty() && methods.isEmpty()) {
			sb.append(" {}");
		} else {
			sb.append(" {\n    // Fields");
			for (VariableModel fieldModel : fields)
				sb.append("\n    ").append(fieldModel.toString());
			sb.append("\n    // Methods");
			for (MethodModel methodModel : methods)
				sb.append("\n    ").append(methodModel.toString());
			sb.append("\n}");
		}
		return sb.toString();
	}
}

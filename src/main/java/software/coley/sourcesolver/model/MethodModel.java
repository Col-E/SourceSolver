package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class MethodModel extends AbstractModel implements Annotated, NameHolder {
	private final String name;
	private final ModifiersModel modifiers;
	private final List<TypeParameterModel> typeParameters;
	private final TypeModel returnType;
	private final List<VariableModel> parameters;
	private final Model defaultValue;
	private final List<AbstractExpressionModel> thrownTypes;
	private final List<AnnotationExpressionModel> annotations;
	private final MethodBodyModel methodBody;

	public MethodModel(@Nonnull Range range,
	                   @Nonnull String name,
	                   @Nonnull ModifiersModel modifiers,
	                   @Nonnull List<TypeParameterModel> typeParameters,
	                   @Nonnull TypeModel returnType,
	                   @Nonnull List<VariableModel> parameters,
	                   @Nullable Model defaultValue,
	                   @Nonnull List<AbstractExpressionModel> thrownTypes,
	                   @Nonnull List<AnnotationExpressionModel> annotations,
	                   @Nullable MethodBodyModel methodBody) {
		super(range, of(modifiers), of(typeParameters), of(returnType), of(parameters), of(defaultValue), of(thrownTypes), of(annotations), of(methodBody));
		this.name = name;
		this.modifiers = modifiers;
		this.typeParameters = typeParameters;
		this.returnType = returnType;
		this.parameters = parameters;
		this.defaultValue = defaultValue;
		this.thrownTypes = thrownTypes;
		this.annotations = annotations;
		this.methodBody = methodBody;
	}

	public boolean isStaticInitializer() {
		return "<clinit>".equals(name);
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nullable
	@Override
	public NameExpressionModel getNameModel() {
		return null;
	}

	@Nonnull
	public ModifiersModel getModifiers() {
		return modifiers;
	}

	@Nonnull
	public List<TypeParameterModel> getTypeParameters() {
		return typeParameters;
	}

	@Nonnull
	public TypeModel getReturnType() {
		return returnType;
	}

	@Nonnull
	public List<VariableModel> getParameters() {
		return parameters;
	}

	@Nullable
	public Model getDefaultValue() {
		return defaultValue;
	}

	@Nonnull
	public List<AbstractExpressionModel> getThrownTypes() {
		return thrownTypes;
	}

	@Nonnull
	public List<AnnotationExpressionModel> getAnnotations() {
		return annotations;
	}

	@Nullable
	public MethodBodyModel getMethodBody() {
		return methodBody;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MethodModel that = (MethodModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!name.equals(that.name)) return false;
		if (!modifiers.equals(that.modifiers)) return false;
		if (!typeParameters.equals(that.typeParameters)) return false;
		if (!returnType.equals(that.returnType)) return false;
		if (!parameters.equals(that.parameters)) return false;
		if (!Objects.equals(defaultValue, that.defaultValue)) return false;
		if (!thrownTypes.equals(that.thrownTypes)) return false;
		if (!Objects.equals(methodBody, that.methodBody)) return false;
		return annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		int result = getReturnType().hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + modifiers.hashCode();
		result = 31 * result + typeParameters.hashCode();
		result = 31 * result + returnType.hashCode();
		result = 31 * result + parameters.hashCode();
		result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
		result = 31 * result + thrownTypes.hashCode();
		result = 31 * result + annotations.hashCode();
		result = 31 * result + (methodBody != null ? methodBody.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (isStaticInitializer()) {
			sb.append("static");
		} else {
			String args = parameters.stream().map(VariableModel::toString).collect(Collectors.joining(" "));
			if (!modifiers.getModifiers().isEmpty())
				sb.append(modifiers).append(' ');
			sb.append(returnType.toString()).append(' ').append(name).append('(').append(args).append(')');
		}

		if (methodBody != null) {
			sb.append(" ").append(methodBody);
		} else {
			sb.append(';');
		}

		return sb.toString();
	}
}

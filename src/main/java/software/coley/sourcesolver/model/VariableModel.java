package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class VariableModel extends AbstractStatementModel implements AnnotatedModel, NameHoldingModel {
	private final List<AnnotationExpressionModel> annotations;
	private final ModifiersModel modifiers;
	private final TypeModel type;
	private final String name;
	private final Model value;

	public VariableModel(@Nonnull Range range,
	                     @Nonnull List<AnnotationExpressionModel> annotations,
	                     @Nonnull ModifiersModel modifiers,
	                     @Nonnull TypeModel type,
	                     @Nonnull String name,
	                     @Nullable Model value) {
		super(range, of(annotations), of(modifiers), of(type), of(value));
		this.annotations = Collections.unmodifiableList(annotations);
		this.modifiers = modifiers;
		this.type = type;
		this.name = name;
		this.value = value;
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
	public TypeModel getType() {
		return type;
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nullable
	@Override
	public NameExpressionModel getNameModel() {
		// Javac doesn't have an AST covering the variable's name
		return null;
	}

	@Nullable
	public Model getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		VariableModel that = (VariableModel) o;

		if (!getRange().equals(that.getRange())) return false;
		if (!annotations.equals(that.annotations)) return false;
		if (!modifiers.equals(that.modifiers)) return false;
		if (!type.equals(that.type)) return false;
		if (!name.equals(that.name)) return false;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + annotations.hashCode();
		result = 31 * result + modifiers.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (modifiers.getModifiers().isEmpty())
			sb.append(modifiers).append(' ');
		sb.append(type).append(' ').append(name);
		if (value != null)
			sb.append(" = ").append(value);
		return sb.toString();
	}
}

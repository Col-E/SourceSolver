package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class VariableModel extends AbstractModel implements Annotated, Named {
	private final List<AnnotationUseModel> annotationModels;
	private final ModifiersModel modifiers;
	private final TypeModel typeModel;
	private final String name;
	private final AbstractModel valueModel;

	public VariableModel(@Nonnull Range range,
	                     @Nonnull List<AnnotationUseModel> annotationModels,
	                     @Nonnull ModifiersModel modifiers,
	                     @Nonnull TypeModel typeModel,
	                     @Nonnull String name,
	                     @Nullable AbstractModel valueModel) {
		super(range, of(annotationModels), of(modifiers), of(typeModel), of(valueModel));

		this.annotationModels = Collections.unmodifiableList(annotationModels);
		this.modifiers = modifiers;
		this.typeModel = typeModel;
		this.name = name;
		this.valueModel = valueModel;
	}

	@Nonnull
	@Override
	public List<AnnotationUseModel> getAnnotationModels() {
		return annotationModels;
	}

	@Nonnull
	public ModifiersModel getModifiers() {
		return modifiers;
	}

	@Nonnull
	public TypeModel getTypeModel() {
		return typeModel;
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nullable
	@Override
	public NameModel getNameModel() {
		// Javac doesn't have an AST covering the variable's name
		return null;
	}

	@Nullable
	public AbstractModel getValueModel() {
		return valueModel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		VariableModel that = (VariableModel) o;

		if (!annotationModels.equals(that.annotationModels)) return false;
		if (!modifiers.equals(that.modifiers)) return false;
		if (!typeModel.equals(that.typeModel)) return false;
		if (!name.equals(that.name)) return false;
		return Objects.equals(valueModel, that.valueModel);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + annotationModels.hashCode();
		result = 31 * result + modifiers.hashCode();
		result = 31 * result + typeModel.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + (valueModel != null ? valueModel.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (modifiers.getModifiers().isEmpty())
			sb.append(modifiers).append(' ');
		sb.append(typeModel).append(' ').append(name);
		if (valueModel != null)
			sb.append(" = ").append(valueModel);
		return sb.toString();
	}
}

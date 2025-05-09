package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.util.Range;

import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class TypeModel extends AbstractModel {
	private final Model identifier;

	protected TypeModel(@Nonnull Range range, @Nonnull Model identifier) {
		super(range, identifier);
		this.identifier = identifier;
	}

	protected TypeModel(@Nonnull Range range, @Nonnull Model identifier,
	                    @Nonnull Collection<? extends Model> additionalChildren) {
		super(range, ChildSupplier.of(identifier), ChildSupplier.of(additionalChildren));
		this.identifier = identifier;
	}

	@Nonnull
	public Model getIdentifier() {
		return identifier;
	}

	@Nonnull
	public abstract Kind getKind();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TypeModel typeModel = (TypeModel) o;

		if (!identifier.equals(typeModel.identifier)) return false;
		return getKind() == typeModel.getKind();
	}

	@Override
	public int hashCode() {
		int result = getRange().hashCode();
		result = 31 * result + identifier.hashCode();
		result = 31 * result + getKind().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return identifier.toString();
	}

	@Nonnull
	public static TypeModel newVar() {
		return new Var();
	}

	public enum Kind {
		PRIMITIVE,
		OBJECT,
		PARAMETERIZED,
		UNION,
		WILDCARD,
		VAR,
		ARRAY,
		ANNOTATED
	}

	public static class Primitive extends TypeModel implements NamedModel {
		private TypeKind primitiveKind;

		public Primitive(@Nonnull Range range, @Nonnull Model identifierModel) {
			super(range, identifierModel);
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.PRIMITIVE;
		}

		@Nonnull
		public TypeKind getPrimitiveKind() {
			if (primitiveKind == null) {
				String identifier = getIdentifier().toString();
				primitiveKind = switch (identifier) {
					case "boolean" -> TypeKind.BOOLEAN;
					case "byte" -> TypeKind.BYTE;
					case "short" -> TypeKind.SHORT;
					case "int" -> TypeKind.INT;
					case "long" -> TypeKind.LONG;
					case "char" -> TypeKind.CHAR;
					case "float" -> TypeKind.FLOAT;
					case "double" -> TypeKind.DOUBLE;
					case "void" -> TypeKind.VOID;
					default -> TypeKind.ERROR;
				};
			}
			return primitiveKind;
		}

		@Nonnull
		@Override
		public String getName() {
			return getIdentifier().toString();
		}
	}

	public static class NamedObject extends TypeModel implements NamedModel {
		public NamedObject(@Nonnull Range range, @Nonnull Model identifierModel) {
			super(range, identifierModel);
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.OBJECT;
		}

		@Nonnull
		@Override
		public String getName() {
			return getIdentifier().toString();
		}
	}

	public static class Array extends TypeModel {
		private final TypeModel elementModel;
		private int dimensions = -1;

		public Array(@Nonnull Range range, @Nonnull TypeModel elementModel) {
			// Identifier holds element type of array
			//
			// int[]   --> int
			// int[][] --> int[]
			//
			// The 'element' type in this case is one less dimension, and not the 'root' type.
			super(range, elementModel);
			this.elementModel = elementModel;
		}

		public int getDimensions() {
			if (dimensions == -1) {
				if (getIdentifier() instanceof Array sub)
					dimensions = 1 + sub.getDimensions();
				else
					dimensions = 1;
			}
			return dimensions;
		}

		@Nonnull
		public TypeModel getRootModel() {
			TypeModel root = elementModel;
			while (root instanceof Array array)
				root = array.getRootModel();
			return root;
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.ARRAY;
		}

		@Override
		public String toString() {
			return getRootModel() + "[]".repeat(getDimensions());
		}
	}

	public static class Annotated extends TypeModel {
		private final TypeModel type;
		private final List<AnnotationExpressionModel> annotations;

		public Annotated(@Nonnull Range range, @Nonnull TypeModel type, @Nonnull List<AnnotationExpressionModel> annotations) {
			super(range, type, annotations);
			this.type = type;
			this.annotations = annotations;
		}

		@Nonnull
		public TypeModel getType() {
			return type;
		}

		@Nonnull
		public List<AnnotationExpressionModel> getAnnotations() {
			return annotations;
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.ANNOTATED;
		}

		@Override
		public String toString() {
			return annotations.stream()
					.map(AnnotationExpressionModel::toString)
					.collect(Collectors.joining(" "))
					+ " "
					+ type.toString();
		}
	}

	public static class Parameterized extends TypeModel implements NamedModel {
		private final List<Model> typeArguments;

		public Parameterized(@Nonnull Range range, @Nonnull Model identifierModel,
		                     @Nonnull List<? extends Model> typeArguments) {
			super(range, identifierModel, typeArguments);
			this.typeArguments = Collections.unmodifiableList(typeArguments);
		}

		@Nonnull
		public List<Model> getTypeArguments() {
			return typeArguments;
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.PARAMETERIZED;
		}

		@Nonnull
		@Override
		public String getName() {
			return getIdentifier().toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;

			Parameterized that = (Parameterized) o;

			return typeArguments.equals(that.typeArguments);
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + typeArguments.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return super.toString() + "<" + typeArguments.stream()
					.map(Object::toString)
					.collect(Collectors.joining(", ")) +
					">";
		}
	}

	public static class Union extends TypeModel {
		private final List<TypeModel> allTypes;

		public Union(@Nonnull Range range, @Nonnull TypeModel identifier,
		             @Nonnull Collection<? extends TypeModel> additionalChildren) {
			super(range, identifier, additionalChildren);
			List<TypeModel> all = new ArrayList<>(additionalChildren.size() + 1);
			all.addAll(additionalChildren);
			all.addFirst(identifier);
			allTypes = Collections.unmodifiableList(all);
		}

		@Nonnull
		public List<TypeModel> getAllTypes() {
			return allTypes;
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.UNION;
		}
	}

	public static class Wildcard extends TypeModel {
		private final Model boundModel;

		public Wildcard(@Nonnull Range range, @Nonnull Model identifierModel,
		                @Nullable Model boundModel) {
			super(range, identifierModel, Collections.singletonList(boundModel));
			this.boundModel = boundModel;
		}

		@Nullable
		public Model getBound() {
			return boundModel;
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.WILDCARD;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;

			Wildcard wildcard = (Wildcard) o;

			return Objects.equals(boundModel, wildcard.boundModel);
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + (boundModel != null ? boundModel.hashCode() : 0);
			return result;
		}
	}

	public static class Var extends TypeModel {
		public Var() {
			super(Range.UNKNOWN, new NameExpressionModel(Range.UNKNOWN, "var"));
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.VAR;
		}
	}
}

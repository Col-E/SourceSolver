package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.type.TypeKind;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class TypeModel extends AbstractModel {
	private final AbstractModel identifier;

	protected TypeModel(@Nonnull Range range, @Nonnull AbstractModel identifier) {
		super(range, identifier);
		this.identifier = identifier;
	}

	protected TypeModel(@Nonnull Range range, @Nonnull AbstractModel identifier,
	                    @Nonnull Collection<? extends AbstractModel> additionalChildren) {
		super(range, ChildSupplier.of(identifier), ChildSupplier.of(additionalChildren));
		this.identifier = identifier;
	}

	@Nonnull
	public AbstractModel getIdentifier() {
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

	public enum Kind {
		PRIMITIVE,
		OBJECT,
		PARAMETERIZED,
		WILDCARD,
		ARRAY
	}

	public static class Primitive extends TypeModel {
		private TypeKind primitiveKind;

		public Primitive(@Nonnull Range range, @Nonnull AbstractModel identifierModel) {
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
	}

	public static class NamedObject extends TypeModel {
		public NamedObject(@Nonnull Range range, @Nonnull AbstractModel identifierModel) {
			super(range, identifierModel);
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.OBJECT;
		}
	}

	public static class Array extends TypeModel {
		private int dimensions = -1;

		public Array(@Nonnull Range range, @Nonnull AbstractModel elementModel) {
			// Identifier holds element type of array
			//
			// int[]   --> int
			// int[][] --> int[]
			//
			// The 'element' type in this case is one less dimension, and not the 'root' type.
			super(range, elementModel);
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
		public AbstractModel getRootModel() {
			AbstractModel root = getIdentifier();
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

	public static class Parameterized extends TypeModel {
		private final List<AbstractModel> typeArguments;

		public Parameterized(@Nonnull Range range, @Nonnull AbstractModel identifierModel,
		                     @Nonnull List<? extends AbstractModel> typeArguments) {
			super(range, identifierModel, typeArguments);
			this.typeArguments = Collections.unmodifiableList(typeArguments);
		}

		@Nonnull
		public List<AbstractModel> getTypeArguments() {
			return typeArguments;
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.PARAMETERIZED;
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

	public static class Wildcard extends TypeModel {
		private final AbstractModel boundModel;

		public Wildcard(@Nonnull Range range, @Nonnull AbstractModel identifierModel,
		                @Nullable AbstractModel boundModel) {
			super(range, identifierModel, Collections.singletonList(boundModel));
			this.boundModel = boundModel;
		}

		@Nullable
		public AbstractModel getBound() {
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
}

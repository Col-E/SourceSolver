package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class TypeModel extends AbstractModel {
	private final AbstractModel identifierModel;

	protected TypeModel(@Nonnull Range range, @Nonnull AbstractModel identifierModel) {
		super(range, identifierModel);
		this.identifierModel = identifierModel;
	}

	@Nonnull
	public AbstractModel getIdentifierModel() {
		return identifierModel;
	}

	@Nonnull
	public abstract Kind getKind();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		TypeModel typeModel = (TypeModel) o;

		if (!identifierModel.equals(typeModel.identifierModel)) return false;
		return getKind() == typeModel.getKind();
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + identifierModel.hashCode();
		result = 31 * result + getKind().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return identifierModel.toString();
	}

	public enum Kind {
		PRIMITIVE,
		OBJECT,
		PARAMETERIZED,
		WILDCARD,
		ARRAY
	}

	public static class Primitive extends TypeModel {
		public Primitive(@Nonnull Range range, @Nonnull AbstractModel identifierModel) {
			super(range, identifierModel);
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.PRIMITIVE;
		}
	}

	public static class Objekt extends TypeModel {
		public Objekt(@Nonnull Range range, @Nonnull AbstractModel identifierModel) {
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
				if (getIdentifierModel() instanceof Array sub)
					dimensions = 1 + sub.getDimensions();
				else
					dimensions = 1;
			}
			return dimensions;
		}

		@Nonnull
		public AbstractModel getRootElementModel() {
			AbstractModel root = getIdentifierModel();
			while (root instanceof Array array)
				root = array.getRootElementModel();
			return root;
		}

		@Nonnull
		@Override
		public Kind getKind() {
			return Kind.ARRAY;
		}

		@Override
		public String toString() {
			return getRootElementModel() + "[]".repeat(getDimensions());
		}
	}

	public static class Parameterized extends TypeModel {
		private final List<AbstractModel> typeArgumentModels;

		public Parameterized(@Nonnull Range range, @Nonnull AbstractModel identifierModel,
		                     @Nonnull List<? extends AbstractModel> typeArgumentModels) {
			super(range, identifierModel);
			this.typeArgumentModels = Collections.unmodifiableList(typeArgumentModels);
		}

		@Nonnull
		public List<AbstractModel> getTypeArgumentModels() {
			return typeArgumentModels;
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

			return typeArgumentModels.equals(that.typeArgumentModels);
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + typeArgumentModels.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return super.toString() + "<" + typeArgumentModels.stream()
					.map(Object::toString)
					.collect(Collectors.joining(", ")) +
					">";
		}
	}

	public static class Wildcard extends TypeModel {
		private final AbstractModel boundModel;

		public Wildcard(@Nonnull Range range, @Nonnull AbstractModel identifierModel,
		                @Nullable AbstractModel boundModel) {
			super(range, identifierModel);
			this.boundModel = boundModel;
		}

		@Nullable
		public AbstractModel getBoundModel() {
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

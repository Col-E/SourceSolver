package software.coley.sourcesolver.resolve.generic;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;

import java.util.List;

/**
 * Generic type model that tracks source-level generic structure separately from erased descriptors so
 * member lookups can adapt fields, returns, and parameters to a bound receiver type.
 *
 * @author Matt Coley
 */
public sealed interface GenericType permits GenericType.ArrayType, GenericType.ClassType,
		GenericType.PrimitiveType, GenericType.TypeVariableType, GenericType.WildcardType {
	/**
	 * @return Erased form of the represented type.
	 */
	@Nonnull
	DescribableEntry asDescribable();

	/**
	 * Primitive generic type such as {@code int} or {@code boolean}.
	 */
	record PrimitiveType(@Nonnull PrimitiveEntry primitive) implements GenericType {
		@Nonnull
		@Override
		public DescribableEntry asDescribable() {
			return primitive;
		}
	}

	/**
	 * Class type with optional type arguments such as {@code List<String>}.
	 */
	record ClassType(@Nonnull ClassEntry classEntry, @Nonnull List<GenericType> typeArguments) implements GenericType {
		public ClassType {
			typeArguments = List.copyOf(typeArguments);
		}

		@Nonnull
		@Override
		public DescribableEntry asDescribable() {
			return classEntry;
		}
	}

	/**
	 * Array type preserving the generic element type and dimension count.
	 */
	record ArrayType(@Nonnull GenericType elementType, int dimensions) implements GenericType {
		@Nonnull
		@Override
		public DescribableEntry asDescribable() {
			return elementType.asDescribable().toArrayEntry(dimensions);
		}
	}

	/**
	 * Reference to a declared type variable such as {@code T}.
	 */
	record TypeVariableType(@Nonnull GenericTypeParameter parameter) implements GenericType {
		@Nonnull
		@Override
		public DescribableEntry asDescribable() {
			return parameter.upperBound();
		}
	}

	/**
	 * Wildcard type such as {@code ?}, {@code ? extends Number}, or {@code ? super String}.
	 */
	record WildcardType(@Nullable GenericType upperBound, @Nullable GenericType lowerBound,
	                    @Nonnull DescribableEntry erasure) implements GenericType {
		@Nonnull
		@Override
		public DescribableEntry asDescribable() {
			return erasure;
		}
	}
}

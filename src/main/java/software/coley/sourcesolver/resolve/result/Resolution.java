package software.coley.sourcesolver.resolve.result;

import javax.annotation.Nonnull;

import static software.coley.sourcesolver.resolve.result.Resolutions.*;

public sealed interface Resolution permits DescribableResolution, PackageResolution, MultiClassResolution, MultiMemberResolution, ThrowingResolution, UnknownResolution {
	default boolean isUnknown() {
		return false;
	}

	@Nonnull
	static Resolution mergeWith(@Nonnull Resolution left, @Nonnull Resolution right) {
		// Merged becomes unknown if either are also unknown.
		if (left.isUnknown() || right.isUnknown())
			return unknown();

		// Merged becomes the wider primitive if both are primitives.
		if (left instanceof PrimitiveResolution primitiveFirst && right instanceof PrimitiveResolution primitiveSecond)
			return primitiveFirst.getDescribableEntry().isAssignableFrom(primitiveSecond.getDescribableEntry()) ?
					primitiveFirst : primitiveSecond;

		// Merged becomes the common parent class.
		if (left instanceof ClassResolution classFirst && right instanceof ClassResolution classSecond)
			return ofClass(classFirst.getClassEntry().getCommonParent(classSecond.getClassEntry()));

		// Merged becomes the common parent class of the array element type.
		//  - Only if the dimension counts are the same.
		if (left instanceof ArrayResolution arrayFirst && right instanceof ArrayResolution arraySecond &&
				arrayFirst.getDescribableEntry().getDimensions() == arraySecond.getDescribableEntry().getDimensions()) {
			Resolution mergedElemenentResolution = mergeWith(arrayFirst.getElementTypeResolution(), arraySecond.getElementTypeResolution());
			if (mergedElemenentResolution instanceof DescribableResolution describableElementResolution)
				return ofArray(describableElementResolution, arrayFirst.getDescribableEntry().getDimensions());
		}

		// Incompatible types therefore we cannot merge.
		return unknown();
	}
}

package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility for creating resolution values.
 *
 * @author Matt Coley
 */
public class Resolutions {
	private static final UnknownResolution UNKNOWN_RESOLUTION = new UnknownResolutionImpl();
	private static final ThrowingResolution THROWS_RESOLUTION = new ThrowingResolutionImpl();
	private static final NullResolution NULL_RESOLUTION = new NullResolutionImpl();

	private Resolutions() {}

	@Nonnull
	public static UnknownResolution unknown() {
		return UNKNOWN_RESOLUTION;
	}

	@Nonnull
	public static ThrowingResolution throwing() {
		return THROWS_RESOLUTION;
	}

	@Nonnull
	public static NullResolution nul() {
		return NULL_RESOLUTION;
	}

	@Nonnull
	public static Resolution ofDescribable(@Nonnull DescribableEntry describable) {
		return switch (describable) {
			case PrimitiveEntry primitiveEntry -> ofPrimitive(primitiveEntry);
			case ClassEntry classEntry -> ofClass(classEntry);
			case ArrayEntry arrayEntry -> ofArray(arrayEntry);
			case NullEntry ignored -> nul();
			case MemberEntry ignored -> unknown(); // Cannot resolve without owner context
		};
	}

	@Nonnull
	public static PrimitiveResolution ofPrimitive(@Nonnull String descriptor) {
		return ofPrimitive(PrimitiveEntry.getPrimitive(descriptor));
	}

	@Nonnull
	public static PrimitiveResolution ofPrimitive(@Nonnull PrimitiveEntry primitive) {
		return new PrimitiveResolutionImpl(primitive);
	}

	@Nonnull
	public static ArrayResolution ofArray(@Nonnull DescribableResolution elementType, int dimensions) {
		return new ArrayResolutionImpl(elementType.getDescribableEntry().toArrayEntry(dimensions));
	}

	@Nonnull
	public static ArrayResolution ofArray(@Nonnull ArrayEntry array) {
		return new ArrayResolutionImpl(array);
	}

	@Nonnull
	public static Resolution ofClass(@Nonnull EntryPool pool, @Nonnull String name) {
		ClassEntry entry = pool.getClass(name);
		if (entry == null)
			return unknown();
		return ofClass(entry);
	}

	@Nonnull
	public static ClassResolution ofClass(@Nonnull ClassEntry entry) {
		return new ClassResolutionImpl(entry);
	}

	@Nonnull
	public static FieldResolution ofField(@Nonnull ClassEntry classEntry, @Nonnull FieldEntry fieldEntry) {
		return new FieldResolutionImpl(classEntry, fieldEntry);
	}

	@Nonnull
	public static Resolution ofField(@Nonnull ClassEntry classEntry, @Nonnull String fieldName, @Nonnull String fieldDescriptor) {
		FieldEntry methodEntry = classEntry.getField(fieldName, fieldDescriptor);
		if (methodEntry != null)
			return new FieldResolutionImpl(classEntry, methodEntry);

		ClassEntry superEntry = classEntry.getSuperEntry();
		if (superEntry != null && ofField(superEntry, fieldName, fieldDescriptor) instanceof FieldResolution resolution)
			return resolution;

		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (ofField(implementedEntry, fieldName, fieldDescriptor) instanceof FieldResolution resolution)
				return resolution;

		return unknown();
	}

	@Nonnull
	public static MethodResolution ofMethod(@Nonnull ClassEntry classEntry, @Nonnull MethodEntry methodEntry) {
		return new MethodResolutionImpl(classEntry, methodEntry);
	}

	@Nonnull
	public static Resolution ofMethod(@Nonnull ClassEntry classEntry, @Nonnull String methodName,
	                                  @Nonnull DescribableEntry returnType, @Nonnull List<? extends DescribableEntry> parameters) {
		String descriptor = '(' + parameters.stream().map(DescribableEntry::getDescriptor).collect(Collectors.joining()) + ')' + returnType.getDescriptor();
		return ofMethod(classEntry, methodName, descriptor);
	}

	@Nonnull
	public static Resolution ofMethod(@Nonnull ClassEntry classEntry, @Nonnull String methodName, @Nonnull String methodDescriptor) {
		MethodEntry methodEntry = classEntry.getMethod(methodName, methodDescriptor);
		if (methodEntry != null)
			return new MethodResolutionImpl(classEntry, methodEntry);

		ClassEntry superEntry = classEntry.getSuperEntry();
		if (superEntry != null && ofMethod(superEntry, methodName, methodDescriptor) instanceof MethodResolution resolution)
			return resolution;

		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (ofMethod(implementedEntry, methodName, methodDescriptor) instanceof MethodResolution resolution)
				return resolution;

		return unknown();
	}

	@Nonnull
	public static MultiClassResolution ofClasses(@Nonnull List<ClassEntry> classEntries) {
		return new MultiClassResolutionImpl(classEntries);
	}

	@Nonnull
	public static MultiMemberResolution ofMembers(@Nonnull List<ClassMemberPair> memberEntries) {
		return new MultiMemberResolutionImpl(memberEntries);
	}

	@Nonnull
	public static Resolution ofMember(@Nonnull ClassMemberPair pair) {
		ClassEntry ownerEntry = pair.ownerEntry();
		if (pair.memberEntry() instanceof FieldEntry fieldEntry)
			return ofField(ownerEntry, fieldEntry);
		else if (pair.memberEntry() instanceof MethodEntry methodEntry)
			return ofMethod(ownerEntry, methodEntry);
		return unknown();
	}

	@Nonnull
	public static Resolution mergeWith(@Nonnull Resolution left, @Nonnull Resolution right) {
		return mergeWith(MergeOp.MERGE_TYPES, left, right);
	}

	@Nonnull
	public static Resolution mergeWith(@Nonnull MergeOp mergeOp, @Nonnull Resolution left, @Nonnull Resolution right) {
		// Edge case for addition/concat in source contexts.
		//  - 1 + 1   --> int
		//  - 1 + "1" --> String
		//  - 1 + 1.0 --> double
		if (mergeOp == MergeOp.ADDITION_OR_CONCAT) {
			Resolution merged = mergeWith(MergeOp.MERGE_TYPES, left, right);
			if (merged.isUnknown()) {
				if (left instanceof ClassResolution leftClass
						&& leftClass.getClassEntry().extendsOrImplementsName("java/lang/String"))
					return leftClass;
				if (right instanceof ClassResolution rightClass
						&& rightClass.getClassEntry().extendsOrImplementsName("java/lang/String"))
					return rightClass;
			}
			return merged;
		}

		// Merged becomes unknown if either are also unknown.
		if (left.isUnknown() || right.isUnknown())
			return unknown();

		// Merged becomes the wider primitive if both are primitives.
		if (left instanceof PrimitiveResolution primitiveFirst && right instanceof PrimitiveResolution primitiveSecond)
			return primitiveFirst.getPrimitiveEntry().isAssignableFrom(primitiveSecond.getPrimitiveEntry()) ?
					primitiveFirst : primitiveSecond;

		// Merged becomes the common parent class.
		if (left instanceof ClassResolution classFirst && right instanceof ClassResolution classSecond)
			return ofClass(classFirst.getClassEntry().getCommonParent(classSecond.getClassEntry()));

		// Merged becomes the common parent class of the array element type.
		//  - Only if the dimension counts are the same.
		if (left instanceof ArrayResolution arrayFirst && right instanceof ArrayResolution arraySecond &&
				arrayFirst.getDimensions() == arraySecond.getDimensions()) {
			Resolution mergedElemenentResolution = mergeWith(arrayFirst.getElementTypeResolution(), arraySecond.getElementTypeResolution());
			if (mergedElemenentResolution instanceof DescribableResolution describableElementResolution)
				return ofArray(describableElementResolution, arrayFirst.getDimensions());
		}

		// Incompatible types therefore we cannot merge.
		return unknown();
	}

	public enum MergeOp {
		MERGE_TYPES, ADDITION_OR_CONCAT
	}

	private record PrimitiveResolutionImpl(@Nonnull PrimitiveEntry primitive) implements PrimitiveResolution {
		@Nonnull
		@Override
		public PrimitiveEntry getPrimitiveEntry() {
			return primitive;
		}
	}

	private record ArrayResolutionImpl(@Nonnull ArrayEntry array) implements ArrayResolution {
		@Nonnull
		@Override
		public ArrayEntry getArrayEntry() {
			return array;
		}

		@Nonnull
		@Override
		public DescribableResolution getElementTypeResolution() {
			DescribableEntry element = array.getElementEntry();
			if (element instanceof ClassEntry classElement)
				return ofClass(classElement);
			else if (element instanceof PrimitiveEntry primitiveElement)
				return ofPrimitive(primitiveElement);
			throw new IllegalStateException("Unknown element type: " + element.getClass().getSimpleName());
		}
	}

	private record MultiClassResolutionImpl(@Nonnull List<ClassEntry> entries) implements MultiClassResolution {
		@Nonnull
		@Override
		public List<ClassEntry> getClassEntries() {
			return entries;
		}
	}

	private record MultiMemberResolutionImpl(
			@Nonnull List<ClassMemberPair> memberEntries) implements MultiMemberResolution {
		@Nonnull
		@Override
		public List<ClassMemberPair> getMemberEntries() {
			return memberEntries;
		}
	}

	private record ClassResolutionImpl(@Nonnull ClassEntry entry) implements ClassResolution {
		@Nonnull
		@Override
		public ClassEntry getClassEntry() {
			return entry;
		}
	}

	private record FieldResolutionImpl(@Nonnull ClassEntry ownerEntry,
	                                   @Nonnull FieldEntry fieldEntry) implements FieldResolution {
		@Nonnull
		@Override
		public ClassEntry getOwnerEntry() {
			return ownerEntry;
		}

		@Nonnull
		@Override
		public FieldEntry getFieldEntry() {
			return fieldEntry;
		}

		@Nonnull
		@Override
		public ClassResolution getOwnerResolution() {
			return ofClass(ownerEntry);
		}
	}

	private record MethodResolutionImpl(@Nonnull ClassEntry ownerEntry,
	                                    @Nonnull MethodEntry methodEntry) implements MethodResolution {
		@Nonnull
		@Override
		public ClassEntry getOwnerEntry() {
			return ownerEntry;
		}

		@Nonnull
		@Override
		public MethodEntry getMethodEntry() {
			return methodEntry;
		}

		@Nonnull
		@Override
		public ClassResolution getOwnerResolution() {
			return ofClass(ownerEntry);
		}
	}

	private record UnknownResolutionImpl() implements UnknownResolution {}

	private record ThrowingResolutionImpl() implements ThrowingResolution {}

	private record NullResolutionImpl() implements NullResolution {}
}

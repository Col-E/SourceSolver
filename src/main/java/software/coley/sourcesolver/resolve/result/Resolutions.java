package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.resolve.entry.ArrayEntry;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MemberEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.entry.NullEntry;
import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;
import software.coley.sourcesolver.resolve.generic.GenericType;
import software.coley.sourcesolver.resolve.generic.GenericTypes;

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
	public static VariableResolution ofVariable(@Nonnull String name, @Nonnull DescribableEntry resolvedType) {
		return ofVariable(name, rawGenericType(resolvedType));
	}

	@Nonnull
	public static VariableResolution ofVariable(@Nonnull String name, @Nonnull GenericType resolvedType) {
		return new VariableResolutionImpl(name, resolvedType);
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
	public static PackageResolution ofPackage(@Nullable String name) {
		return new PackageResolutionImpl(name);
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
		return ofClass(GenericTypes.ofClass(entry));
	}

	@Nonnull
	public static ClassResolution ofClass(@Nonnull GenericType.ClassType classType) {
		return new ClassResolutionImpl(classType);
	}

	@Nonnull
	public static FieldResolution ofField(@Nonnull ClassEntry classEntry, @Nonnull FieldEntry fieldEntry) {
		return ofField(GenericTypes.ofClass(classEntry), fieldEntry, fieldEntry.getGenericType());
	}

	@Nonnull
	public static FieldResolution ofField(@Nonnull GenericType.ClassType ownerType, @Nonnull FieldEntry fieldEntry,
	                                      @Nonnull GenericType resolvedFieldType) {
		return new FieldResolutionImpl(ownerType, fieldEntry, resolvedFieldType);
	}

	@Nonnull
	public static Resolution ofField(@Nonnull ClassEntry classEntry, @Nonnull String fieldName, @Nonnull String fieldDescriptor) {
		FieldEntry methodEntry = classEntry.getDeclaredField(fieldName, fieldDescriptor);
		if (methodEntry != null)
			return ofField(classEntry, methodEntry);

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
		return ofMethod(GenericTypes.ofClass(classEntry), methodEntry,
				methodEntry.getGenericReturnType(), methodEntry.getGenericParameterTypes());
	}

	@Nonnull
	public static MethodResolution ofMethod(@Nonnull GenericType.ClassType ownerType, @Nonnull MethodEntry methodEntry,
	                                        @Nonnull GenericType resolvedReturnType,
	                                        @Nonnull List<GenericType> resolvedParameterTypes) {
		return new MethodResolutionImpl(ownerType, methodEntry, resolvedReturnType, resolvedParameterTypes);
	}

	@Nonnull
	public static Resolution ofMethod(@Nonnull ClassEntry classEntry, @Nonnull String methodName,
	                                  @Nonnull DescribableEntry returnType, @Nonnull List<? extends DescribableEntry> parameters) {
		String descriptor = '(' + parameters.stream().map(DescribableEntry::getDescriptor).collect(Collectors.joining()) + ')' + returnType.getDescriptor();
		return ofMethod(classEntry, methodName, descriptor);
	}

	@Nonnull
	public static Resolution ofMethod(@Nonnull ClassEntry classEntry, @Nonnull String methodName, @Nonnull String methodDescriptor) {
		MethodEntry methodEntry = classEntry.getDeclaredMethod(methodName, methodDescriptor);
		if (methodEntry != null)
			return ofMethod(classEntry, methodEntry);

		ClassEntry superEntry = classEntry.getSuperEntry();
		if (superEntry != null && ofMethod(superEntry, methodName, methodDescriptor) instanceof MethodResolution resolution)
			return resolution;

		for (ClassEntry implementedEntry : classEntry.getImplementedEntries())
			if (ofMethod(implementedEntry, methodName, methodDescriptor) instanceof MethodResolution resolution)
				return resolution;

		return unknown();
	}

	@Nonnull
	public static Resolution ofClasses(@Nonnull List<ClassEntry> classEntries) {
		if (classEntries.isEmpty())
			return unknown();
		return new MultiClassResolutionImpl(classEntries);
	}

	@Nonnull
	public static Resolution ofMembers(@Nonnull List<ClassMemberPair> memberEntries) {
		if (memberEntries.isEmpty())
			return unknown();
		return new MultiMemberResolutionImpl(memberEntries);
	}

	@Nonnull
	public static Resolution ofMember(@Nonnull ClassMemberPair pair) {
		return ofMember(pair.ownerEntry(), pair.memberEntry());
	}

	@Nonnull
	public static Resolution ofMember(@Nonnull ClassEntry ownerEntry, @Nonnull MemberEntry memberEntry) {
		if (memberEntry instanceof FieldEntry fieldEntry)
			return ofField(ownerEntry, fieldEntry);
		else if (memberEntry instanceof MethodEntry methodEntry)
			return ofMethod(ownerEntry, methodEntry);
		return unknown();
	}

	@Nonnull
	public static GenericType.ClassType getResolvedClassType(@Nonnull ClassResolution resolution) {
		if (resolution instanceof ClassResolutionImpl classResolution)
			return classResolution.classType();
		return GenericTypes.ofClass(resolution.getClassEntry());
	}

	@Nonnull
	public static GenericType getResolvedFieldGenericType(@Nonnull FieldResolution resolution) {
		if (resolution instanceof FieldResolutionImpl fieldResolution)
			return fieldResolution.resolvedFieldType();
		return resolution.getFieldEntry().getGenericType();
	}

	@Nonnull
	public static GenericType getResolvedMethodReturnGenericType(@Nonnull MethodResolution resolution) {
		if (resolution instanceof MethodResolutionImpl methodResolution)
			return methodResolution.resolvedReturnType();
		return resolution.getMethodEntry().getGenericReturnType();
	}

	@Nonnull
	public static List<GenericType> getResolvedMethodParameterGenericTypes(@Nonnull MethodResolution resolution) {
		if (resolution instanceof MethodResolutionImpl methodResolution)
			return methodResolution.resolvedParameterTypes();
		return resolution.getMethodEntry().getGenericParameterTypes();
	}

	@Nonnull
	public static GenericType getResolvedVariableGenericType(@Nonnull VariableResolution resolution) {
		if (resolution instanceof VariableResolutionImpl variableResolution)
			return variableResolution.resolvedType();
		return rawGenericType(resolution.getResolvedType());
	}

	/**
	 * @param resolution
	 * 		Some resolution.
	 *
	 * @return The resolved value type represented by the resolution, or {@code null} when it does not describe a value type.
	 */
	@Nullable
	public static DescribableEntry getResolvedValueType(@Nonnull Resolution resolution) {
		return switch (resolution) {
			case VariableResolution variableResolution -> variableResolution.getResolvedType();
			case FieldResolution fieldResolution -> fieldResolution.getResolvedFieldType();
			case MethodResolution methodResolution -> methodResolution.getResolvedReturnType();
			case DescribableResolution describableResolution -> describableResolution.getDescribableEntry();
			default -> null;
		};
	}

	/**
	 * @param resolution
	 * 		Some resolution.
	 *
	 * @return The resolved type represented by the resolution, or {@code null} when it does not describe a type.
	 */
	@Nullable
	public static DescribableEntry getResolvedType(@Nonnull Resolution resolution) {
		DescribableEntry valueType = getResolvedValueType(resolution);
		if (valueType != null)
			return valueType;
		return switch (resolution) {
			case ClassResolution classResolution -> classResolution.getClassEntry();
			case DescribableResolution describableResolution -> describableResolution.getDescribableEntry();
			default -> null;
		};
	}

	/**
	 * @param resolution
	 * 		Some resolution.
	 *
	 * @return Resolution of the value type represented by the input resolution.
	 */
	@Nonnull
	public static Resolution toValueTypeResolution(@Nonnull Resolution resolution) {
		DescribableEntry type = getResolvedValueType(resolution);
		return type == null ? unknown() : ofDescribable(type);
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
		left = toMergeValueResolution(left);
		right = toMergeValueResolution(right);
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

	@Nonnull
	private static Resolution toMergeValueResolution(@Nonnull Resolution resolution) {
		// Flatten resolutions to their value type resolution.
		//  - Fields    -> Variable type
		//  - Variables -> Variable  type
		//  - Methods   -> Return type
		return switch (resolution) {
			case VariableResolution ignored -> toValueTypeResolution(resolution);
			case FieldResolution ignored -> toValueTypeResolution(resolution);
			case MethodResolution ignored -> toValueTypeResolution(resolution);
			default -> resolution;
		};
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

	private record MultiMemberResolutionImpl(@Nonnull List<ClassMemberPair> memberEntries) implements MultiMemberResolution {
		@Nonnull
		@Override
		public List<ClassMemberPair> getMemberEntries() {
			return memberEntries;
		}
	}

	private record ClassResolutionImpl(@Nonnull GenericType.ClassType classType) implements ClassResolution {
		@Nonnull
		@Override
		public ClassEntry getClassEntry() {
			return classType.classEntry();
		}
	}

	private record FieldResolutionImpl(@Nonnull GenericType.ClassType ownerType,
	                                   @Nonnull FieldEntry fieldEntry,
	                                   @Nonnull GenericType resolvedFieldType) implements FieldResolution {
		@Nonnull
		@Override
		public ClassEntry getOwnerEntry() {
			return ownerType.classEntry();
		}

		@Nonnull
		@Override
		public FieldEntry getFieldEntry() {
			return fieldEntry;
		}

		@Nonnull
		@Override
		public DescribableEntry getResolvedFieldType() {
			return GenericTypes.toUsableType(resolvedFieldType, ownerType.classEntry()).asDescribable();
		}

		@Nonnull
		@Override
		public ClassResolution getOwnerResolution() {
			return ofClass(ownerType);
		}
	}

	private record MethodResolutionImpl(@Nonnull GenericType.ClassType ownerType,
	                                    @Nonnull MethodEntry methodEntry,
	                                    @Nonnull GenericType resolvedReturnType,
	                                    @Nonnull List<GenericType> resolvedParameterTypes) implements MethodResolution {
		private MethodResolutionImpl {
			resolvedParameterTypes = List.copyOf(resolvedParameterTypes);
		}

		@Nonnull
		@Override
		public ClassEntry getOwnerEntry() {
			return ownerType.classEntry();
		}

		@Nonnull
		@Override
		public MethodEntry getMethodEntry() {
			return methodEntry;
		}

		@Nonnull
		@Override
		public DescribableEntry getResolvedReturnType() {
			return GenericTypes.toUsableType(resolvedReturnType, ownerType.classEntry()).asDescribable();
		}

		@Nonnull
		@Override
		public ClassResolution getOwnerResolution() {
			return ofClass(ownerType);
		}
	}

	private record VariableResolutionImpl(@Nonnull String name, @Nonnull GenericType resolvedType) implements VariableResolution {
		@Nonnull
		@Override
		public String getName() {
			return name;
		}

		@Nonnull
		@Override
		public DescribableEntry getResolvedType() {
			return resolvedType.asDescribable();
		}
	}

	private record UnknownResolutionImpl() implements UnknownResolution {}

	private record ThrowingResolutionImpl() implements ThrowingResolution {}

	private record NullResolutionImpl() implements NullResolution {}

	private record PackageResolutionImpl(@Nullable String name) implements PackageResolution {
		@Nullable
		@Override
		public String getPackageName() {
			return name;
		}
	}

	@Nonnull
	private static GenericType rawGenericType(@Nonnull DescribableEntry entry) {
		return switch (entry) {
			case ClassEntry classEntry -> GenericTypes.ofClass(classEntry);
			case PrimitiveEntry primitiveEntry -> GenericTypes.ofPrimitive(primitiveEntry);
			case ArrayEntry arrayEntry -> new GenericType.ArrayType(rawGenericType(arrayEntry.getElementEntry()), arrayEntry.getDimensions());
			case NullEntry ignored -> throw new IllegalArgumentException("Variables cannot resolve to null type");
			case MemberEntry ignored -> throw new IllegalArgumentException("Variables cannot resolve to member type");
		};
	}
}

package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ArrayEntry;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

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
	public static PrimitiveResolution ofPrimitive(@Nonnull String descriptor) {
		return ofPrimitive(PrimitiveEntry.getPrimitive(descriptor));
	}

	@Nonnull
	public static PrimitiveResolution ofPrimitive(@Nonnull PrimitiveEntry primitive) {
		return new PrimitiveResolutionImpl(primitive);
	}

	@Nonnull
	public static ArrayResolution ofArray(@Nonnull DescribableResolution elementType, int dimensions) {
		return new ArrayResolutionImpl(ArrayEntry.getArray(dimensions, elementType.getDescribableEntry()));
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

	private record PrimitiveResolutionImpl(@Nonnull PrimitiveEntry primitive) implements PrimitiveResolution {
		@Nonnull
		@Override
		public PrimitiveEntry getDescribableEntry() {
			return primitive;
		}
	}

	private record ArrayResolutionImpl(@Nonnull ArrayEntry array) implements ArrayResolution {
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

		@Nonnull
		@Override
		public ArrayEntry getDescribableEntry() {
			return array;
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
	}

	private record UnknownResolutionImpl() implements UnknownResolution {}

	private record ThrowingResolutionImpl() implements ThrowingResolution {}

	private record NullResolutionImpl() implements NullResolution {}
}

package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class Resolutions {
	private static final UnknownResolution INSTANCE = new UnknownResolution() {};

	private Resolutions() {}

	@Nonnull
	public static UnknownResolution unknown() {
		return INSTANCE;
	}

	@Nonnull
	public static PrimitiveResolution ofPrimitive(@Nonnull String desc) {
		if (desc.length() != 1) throw new IllegalStateException("Not a primitive descriptor: " + desc);
		return switch (desc.charAt(0)) {
			case 'Z' -> PrimitiveResolutionImpl.BOOLEAN;
			case 'B' -> PrimitiveResolutionImpl.BYTE;
			case 'S' -> PrimitiveResolutionImpl.SHORT;
			case 'I' -> PrimitiveResolutionImpl.INT;
			case 'J' -> PrimitiveResolutionImpl.LONG;
			case 'C' -> PrimitiveResolutionImpl.CHAR;
			case 'F' -> PrimitiveResolutionImpl.FLOAT;
			case 'D' -> PrimitiveResolutionImpl.DOUBLE;
			case 'V' -> PrimitiveResolutionImpl.VOID;
			default -> throw new IllegalStateException("Invalid primitive descriptor: " + desc);
		};
	}

	@Nonnull
	public static ArrayResolution ofArray(@Nonnull DescribableResolution elementType, int dimensions) {
		return new ArrayResolutionImpl(elementType, dimensions);
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

	private record PrimitiveResolutionImpl(@Nonnull String desc) implements PrimitiveResolution, DescribableEntry {
		private static final PrimitiveResolution BOOLEAN = new PrimitiveResolutionImpl("Z");
		private static final PrimitiveResolution BYTE = new PrimitiveResolutionImpl("B");
		private static final PrimitiveResolution SHORT = new PrimitiveResolutionImpl("S");
		private static final PrimitiveResolution INT = new PrimitiveResolutionImpl("I");
		private static final PrimitiveResolution LONG = new PrimitiveResolutionImpl("J");
		private static final PrimitiveResolution CHAR = new PrimitiveResolutionImpl("C");
		private static final PrimitiveResolution FLOAT = new PrimitiveResolutionImpl("F");
		private static final PrimitiveResolution DOUBLE = new PrimitiveResolutionImpl("D");
		private static final PrimitiveResolution VOID = new PrimitiveResolutionImpl("V");

		@Nonnull
		@Override
		public DescribableEntry getDescribableEntry() {
			return this;
		}

		@Nonnull
		@Override
		public String getDescriptor() {
			return desc;
		}
	}

	private record ArrayResolutionImpl(@Nonnull DescribableResolution elementResolution,
	                                   int dimensions) implements ArrayResolution, DescribableEntry {
		@Nonnull
		@Override
		public DescribableResolution getElementTypeResolution() {
			return elementResolution;
		}

		@Nonnull
		@Override
		public DescribableEntry getDescribableEntry() {
			return this;
		}

		@Nonnull
		@Override
		public String getDescriptor() {
			return "[".repeat(dimensions) + elementResolution.getDescribableEntry().getDescriptor();
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
}

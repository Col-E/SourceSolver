package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class Resolutions {
	private Resolutions() {}

	@Nonnull
	public static Resolution ofClass(@Nonnull EntryPool pool, @Nonnull String name) {
		ClassEntry entry = pool.getClass(name);
		if (entry == null)
			return UnknownResolution.INSTANCE;
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

		return UnknownResolution.INSTANCE;
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

		return UnknownResolution.INSTANCE;
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

package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface EntryPool {
	@Nonnull
	EntryPool copy();

	void register(@Nonnull ClassEntry entry);

	@Nullable
	default DescribableEntry getDescribable(@Nonnull String descriptor) {
		if (descriptor.isEmpty())
			throw new IllegalStateException("Illegal empty descriptor");

		// Primitive
		if (descriptor.length() == 1)
			return PrimitiveEntry.getPrimitive(descriptor);

		// Array
		if (descriptor.charAt(0) == '[') {
			int dimensions = descriptor.lastIndexOf('[') + 1;
			DescribableEntry element = getDescribable(descriptor.substring(dimensions));
			if (element != null)
				return ArrayEntry.getArray(dimensions, element);

			// Element type not resolvable
			return null;
		}

		// Class
		if (descriptor.charAt(0) == 'L' && descriptor.charAt(descriptor.length() - 1) == ';')
			return getClass(descriptor.substring(1, descriptor.length() - 1));

		return null;
	}

	@Nullable
	ClassEntry getClass(@Nonnull String name);

	@Nonnull
	List<ClassEntry> getClassesInPackage(@Nullable String packageName);
}

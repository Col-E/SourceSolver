package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Outline of a pool that provides access to class metadata.
 *
 * @author Matt Coley
 */
public interface EntryPool {
	/**
	 * Adds the given class to the current pool.
	 *
	 * @param entry
	 * 		Class to add.
	 */
	void register(@Nonnull ClassEntry entry);

	/**
	 * @param descriptor
	 * 		Descriptor of some kind <i>(primitive, array, or class)</i>
	 *
	 * @return Entry of the parsed primitive, array, or class type.
	 * {@code null} if the descriptor could not be mapped to a known entry.
	 */
	@Nullable
	default DescribableEntry getDescribable(@Nonnull String descriptor) {
		if (descriptor.isEmpty())
			return null;

		// Primitive
		if (descriptor.length() == 1)
			return PrimitiveEntry.getPrimitive(descriptor);

		// Array
		if (descriptor.charAt(0) == '[') {
			int dimensions = descriptor.lastIndexOf('[') + 1;
			DescribableEntry element = getDescribable(descriptor.substring(dimensions));
			if (element != null)
				return element.toArrayEntry(dimensions);

			// Element type not resolvable
			return null;
		}

		// Class
		if (descriptor.charAt(0) == 'L' && descriptor.charAt(descriptor.length() - 1) == ';')
			return getClass(descriptor.substring(1, descriptor.length() - 1));

		return null;
	}

	/**
	 * @param name
	 * 		Internal class name. For instance {@code "java/lang/String"}.
	 *
	 * @return Entry of the class, if found within this pool.
	 */
	@Nullable
	ClassEntry getClass(@Nonnull String name);

	/**
	 * @param packageName
	 * 		Internal package name. For instance {@code "java/lang"}.
	 *
	 * @return List of all entries in the package <i>(but not sub-packages)</i> that are known to this pool.
	 */
	@Nonnull
	List<ClassEntry> getClassesInPackage(@Nullable String packageName);
}

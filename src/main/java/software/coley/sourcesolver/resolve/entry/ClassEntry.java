package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Metadata model of a class type.
 */
public non-sealed interface ClassEntry extends AccessedEntry, DescribableEntry {
	/**
	 * @return Metadata model of the super-type, if available.
	 */
	@Nullable
	ClassEntry getSuperEntry();

	/**
	 * @return Metadata models of all implemented interface types.
	 */
	@Nonnull
	List<ClassEntry> getImplementedEntries();

	/**
	 * @return Name of class in internal format.
	 */
	@Nonnull
	String getName();

	@Nonnull
	@Override
	default String getDescriptor() {
		return 'L' + getName() + ';';
	}

	default boolean isInterface() {
		return (getAccess() & Modifier.INTERFACE) != 0;
	}

	/**
	 * @return Metadata models of all declared fields.
	 */
	@Nonnull
	List<FieldEntry> getFields();


	/**
	 * @return Metadata models of all declared methods.
	 */
	@Nonnull
	List<MethodEntry> getMethods();


	/**
	 * @return Stream of all metadata models for declared fields and methods.
	 */
	@Nonnull
	default Stream<MemberEntry> memberStream() {
		return Stream.concat(getFields().stream(), getMethods().stream());
	}

	/**
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 *
	 * @return Metadata model of the declared field, or {@code null} if no such field exists in this class.
	 */
	@Nullable
	default FieldEntry getField(@Nonnull String name, @Nonnull String desc) {
		return getField(name, desc, null);
	}

	/**
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 * @param filter
	 * 		Optional filter to limit matches.
	 *
	 * @return Metadata model of the declared field, or {@code null} if no such field exists in this class.
	 */
	@Nullable
	default FieldEntry getField(@Nonnull String name, @Nonnull String desc, @Nullable Predicate<FieldEntry> filter) {
		for (FieldEntry field : getFields())
			if (field.getName().equals(name) && field.getDescriptor().equals(desc) && (filter == null || filter.test(field)))
				return field;
		ClassEntry superEntry = getSuperEntry();
		if (superEntry != null) {
			FieldEntry superField = superEntry.getField(name, desc, m -> !m.isPrivate());
			if (superField != null)
				return superField;
		}
		for (ClassEntry implementedEntry : getImplementedEntries()) {
			FieldEntry superField = implementedEntry.getField(name, desc, m -> !m.isPrivate());
			if (superField != null)
				return superField;
		}
		return null;
	}

	/**
	 * @param name
	 * 		Field name.
	 *
	 * @return All declared fields with the given name.
	 */
	@Nonnull
	default List<FieldEntry> getFieldsByName(@Nonnull String name) {
		List<FieldEntry> matched = new ArrayList<>();
		for (FieldEntry field : getFields())
			if (field.getName().equals(name))
				matched.add(field);
		return matched;
	}

	/**
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 *
	 * @return Metadata model of the declared method, or {@code null} if no such method exists in this class.
	 */
	@Nullable
	default MethodEntry getMethod(@Nonnull String name, @Nonnull String desc) {
		return getMethod(name, desc, null);
	}

	/**
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method descriptor.
	 * @param filter
	 * 		Optional filter to limit matches.
	 *
	 * @return Metadata model of the declared method, or {@code null} if no such method exists in this class.
	 */
	@Nullable
	default MethodEntry getMethod(@Nonnull String name, @Nonnull String desc, @Nullable Predicate<MethodEntry> filter) {
		for (MethodEntry method : getMethods())
			if (method.getName().equals(name) && method.getDescriptor().equals(desc) && (filter == null || filter.test(method)))
				return method;
		ClassEntry superEntry = getSuperEntry();
		if (superEntry != null) {
			MethodEntry superMethod = superEntry.getMethod(name, desc, m -> !m.isPrivate());
			if (superMethod != null)
				return superMethod;
		}
		for (ClassEntry implementedEntry : getImplementedEntries()) {
			MethodEntry superMethod = implementedEntry.getMethod(name, desc, m -> !m.isPrivate());
			if (superMethod != null)
				return superMethod;
		}
		return null;
	}

	/**
	 * @param name
	 * 		Method name.
	 *
	 * @return All declared methods with the given name.
	 */
	@Nonnull
	default List<MethodEntry> getMethodsByName(@Nonnull String name) {
		List<MethodEntry> matched = new ArrayList<>();
		for (MethodEntry method : getMethods())
			if (method.getName().equals(name))
				matched.add(method);
		return matched;
	}

	/**
	 * Visits the current class, and all parent classes <i>(extended or implemented)</i>.
	 *
	 * @param consumer
	 * 		Consumer to visit each class.
	 */
	default void visitHierarchy(@Nonnull Consumer<ClassEntry> consumer) {
		consumer.accept(this);
		ClassEntry superEntry = getSuperEntry();
		if (superEntry != null)
			superEntry.visitHierarchy(consumer);
		for (ClassEntry implementedEntry : getImplementedEntries())
			implementedEntry.visitHierarchy(consumer);
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return {@code true} if this class extends or implements the requested class.
	 */
	default boolean extendsOrImplementsName(@Nonnull String name) {
		if (getName().equals(name))
			return true;
		if (getSuperEntry() != null && getSuperEntry().extendsOrImplementsName(name))
			return true;
		for (ClassEntry implementedEntry : getImplementedEntries())
			if (implementedEntry.extendsOrImplementsName(name))
				return true;
		return false;
	}

	@Override
	default boolean isAssignableFrom(@Nonnull DescribableEntry other) {
		// Any null value can be assigned to a class value type
		if (other instanceof NullEntry)
			return true;

		// Must be assignable
		if (other instanceof ClassEntry otherClass)
			return isAssignableFrom(otherClass);

		return false;
	}

	default boolean isAssignableFrom(@Nonnull ClassEntry child) {
		// If our names match, or we are object the child's type can be assigned to ours.
		if (Objects.equals(getName(), child.getName()) || getName().equals("java/lang/Object"))
			return true;

		// Check if their parent is assignable to our type.
		ClassEntry superEntry = child.getSuperEntry();
		if (superEntry != null && isAssignableFrom(superEntry))
			return true;

		// Check if their super-interfaces are assignable to our type.
		for (ClassEntry implementedEntry : child.getImplementedEntries())
			if (isAssignableFrom(implementedEntry))
				return true;

		return false;
	}

	/**
	 * @param other
	 * 		Some other class entry.
	 *
	 * @return Common parent type shared between the two.
	 */
	@Nonnull
	default ClassEntry getCommonParent(@Nonnull ClassEntry other) {
		// Check if we are the common parent, or if they are.
		if (isAssignableFrom(other))
			return this;
		if (other.isAssignableFrom(this))
			return other;

		// Yield object as common type if we are both interfaces and are not assignable in either direction.
		if (isInterface() || other.isInterface()) {
			while (!other.getName().equals("java/lang/Object")) {
				other = other.getSuperEntry();
				if (other == null)
					throw new IllegalStateException("Object not found in class hierarchy");
			}
			return other;
		}

		// Check again as the parent class.
		ClassEntry superEntry = getSuperEntry();
		if (superEntry != null)
			return superEntry.getCommonParent(other);
		throw new IllegalStateException("Could not compute common parent, did not observe 'java/lang/Object' boundary");
	}
}

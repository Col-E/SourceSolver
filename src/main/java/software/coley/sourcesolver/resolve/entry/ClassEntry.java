package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public non-sealed interface ClassEntry extends AccessedEntry, DescribableEntry {
	@Nullable
	ClassEntry getSuperEntry();

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

	@Nonnull
	List<FieldEntry> getFields();

	@Nonnull
	List<MethodEntry> getMethods();

	@Nonnull
	default Stream<MemberEntry> memberStream() {
		return Stream.concat(getFields().stream(), getMethods().stream());
	}

	@Nullable
	default FieldEntry getField(@Nonnull String name, @Nonnull String desc) {
		return getField(name, desc, null);
	}

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

	@Nonnull
	default List<FieldEntry> getFieldsByName(@Nonnull String name) {
		List<FieldEntry> matched = new ArrayList<>();
		for (FieldEntry field : getFields())
			if (field.getName().equals(name))
				matched.add(field);
		return matched;
	}

	@Nonnull
	default Map<String, FieldEntry> getDistinctFieldsByNameInHierarchy(@Nonnull String name) {
		Map<String, FieldEntry> fields = new TreeMap<>();
		visitHierarchy(cls -> {
			for (FieldEntry field : cls.getFields())
				if (field.getName().equals(name))
					fields.put(field.getDescriptor() + ' ' + field.getName(), field);
		});
		return fields;
	}

	@Nullable
	default MethodEntry getMethod(@Nonnull String name, @Nonnull String desc) {
		return getMethod(name, desc, null);
	}

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

	@Nonnull
	default List<MethodEntry> getMethodsByName(@Nonnull String name) {
		List<MethodEntry> matched = new ArrayList<>();
		for (MethodEntry method : getMethods())
			if (method.getName().equals(name))
				matched.add(method);
		return matched;
	}

	default void visitHierarchy(@Nonnull Consumer<ClassEntry> consumer) {
		consumer.accept(this);
		ClassEntry superEntry = getSuperEntry();
		if (superEntry != null)
			superEntry.visitHierarchy(consumer);
		for (ClassEntry implementedEntry : getImplementedEntries())
			implementedEntry.visitHierarchy(consumer);
	}

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

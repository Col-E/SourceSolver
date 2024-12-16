package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ClassEntry extends AccessedEntry, DescribableEntry {
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
	List<FieldEntry> getFields();

	@Nonnull
	List<MethodEntry> getMethods();

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

	@Nonnull
	default Map<String, MethodEntry> getDistinctMethodsByNameInHierarchy(@Nonnull String name) {
		Map<String, MethodEntry> methods = new TreeMap<>();
		visitHierarchy(cls -> {
			for (MethodEntry method : cls.getMethods())
				if (method.getName().equals(name))
					methods.put(method.getDescriptor() + ' ' + method.getName(), method);
		});
		return methods;
	}

	default void visitHierarchy(@Nonnull Consumer<ClassEntry> consumer) {
		consumer.accept(this);
		ClassEntry superEntry = getSuperEntry();
		if (superEntry != null)
			superEntry.visitHierarchy(consumer);
		for (ClassEntry implementedEntry : getImplementedEntries()) {
			implementedEntry.visitHierarchy(consumer);
		}
	}

	@Nonnull
	@Override
	default String getDescriptor() {
		return 'L' + getName() + ';';
	}
}

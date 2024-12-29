package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class entry implementation that is populated via reflection.
 *
 * @author Matt Coley
 */
public class ReflectiveClassEntry extends BasicClassEntry {
	private ReflectiveClassEntry(@Nonnull String className,
	                             int access,
	                             @Nullable ClassEntry superEntry,
	                             @Nonnull List<ClassEntry> interfaceEntries,
	                             @Nonnull List<ClassEntry> innerClassEntries,
	                             @Nonnull List<FieldEntry> fields,
	                             @Nonnull List<MethodEntry> methods) {
		super(className, access, superEntry, interfaceEntries, innerClassEntries, fields, methods);
	}

	/**
	 * @param cls
	 * 		Class to create an entry for.
	 *
	 * @return Class entry modeling the class.
	 */
	@Nonnull
	public static ClassEntry build(@Nonnull Map<String, ClassEntry> cache, @Nonnull Class<?> cls) {
		String className = cls.getName().replace('.', '/');
		ClassEntry cached = cache.get(className);
		if (cached != null)
			return cached;

		List<FieldEntry> fields = new ArrayList<>();
		List<MethodEntry> methods = new ArrayList<>();
		for (Field field : cls.getDeclaredFields()) {
			String fieldName = field.getName();
			String fieldDescriptor = field.getType().descriptorString();
			int modifiers = field.getModifiers();
			fields.add(new BasicFieldEntry(fieldName, fieldDescriptor, modifiers));
		}
		for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
			String methodDescriptor = MethodType.methodType(void.class, constructor.getParameterTypes()).descriptorString();
			int modifiers = constructor.getModifiers();
			methods.add(new BasicMethodEntry("<init>", methodDescriptor, modifiers));
		}
		for (Method method : cls.getDeclaredMethods()) {
			String methodName = method.getName();
			String methodDescriptor = MethodType.methodType(method.getReturnType(), method.getParameterTypes()).descriptorString();
			int modifiers = method.getModifiers();
			methods.add(new BasicMethodEntry(methodName, methodDescriptor, modifiers));
		}
		Class<?> superClass = cls.getSuperclass();
		Class<?>[] interfaces = cls.getInterfaces();
		ClassEntry superEntry = superClass == null ? null : build(cache, superClass);
		List<ClassEntry> interfaceEntries = new ArrayList<>(interfaces.length);
		for (Class<?> implemented : interfaces)
			interfaceEntries.add(build(cache, implemented));
		Class<?>[] innerClasses = cls.getDeclaredClasses();
		List<ClassEntry> innerClassEntries = new ArrayList<>();
		int modifiers = cls.getModifiers();
		ClassEntry entry = new BasicClassEntry(className, modifiers, superEntry, interfaceEntries, innerClassEntries, fields, methods);
		cache.put(className, entry);

		// I know this is REALLY cringe putting the inner class population AFTER the building of the model,
		// but if we don't do this we run the risk of running into a cycle.
		for (Class<?> innerClass : innerClasses)
			if (innerClass.getName().startsWith(cls.getName() + "$") && !innerClass.getName().equals(cls.getName()))
				innerClassEntries.add(build(cache, innerClass));

		return entry;
	}
}

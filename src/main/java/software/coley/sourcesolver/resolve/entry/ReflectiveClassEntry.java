package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectiveClassEntry extends BasicClassEntry {
	private ReflectiveClassEntry(@Nonnull String className,
	                             int access,
	                             @Nullable ClassEntry superEntry,
	                             @Nonnull List<ClassEntry> interfaceEntries,
	                             @Nonnull List<FieldEntry> fields,
	                             @Nonnull List<MethodEntry> methods) {
		super(className, access, superEntry, interfaceEntries, fields, methods);
	}

	@Nonnull
	public static ClassEntry build(@Nonnull Class<?> ref) {
		String className = ref.getName().replace('.', '/');
		List<FieldEntry> fields = new ArrayList<>();
		List<MethodEntry> methods = new ArrayList<>();
		for (Field field : ref.getDeclaredFields()) {
			String fieldName = field.getName();
			String fieldDescriptor = field.getType().descriptorString();
			int modifiers = field.getModifiers();
			fields.add(new BasicFieldEntry(fieldName, fieldDescriptor, modifiers));
		}
		for (Constructor<?> constructor : ref.getConstructors()) {
			String methodDescriptor = MethodType.methodType(void.class, constructor.getParameterTypes()).descriptorString();
			int modifiers = constructor.getModifiers();
			methods.add(new BasicMethodEntry("<init>", methodDescriptor, modifiers));
		}
		for (Method method : ref.getDeclaredMethods()) {
			String methodName = method.getName();
			String methodDescriptor = MethodType.methodType(method.getReturnType(), method.getParameterTypes()).descriptorString();
			int modifiers = method.getModifiers();
			methods.add(new BasicMethodEntry(methodName, methodDescriptor, modifiers));
		}
		Class<?> superClass = ref.getSuperclass();
		Class<?>[] interfaces = ref.getInterfaces();
		ClassEntry superEntry = superClass == null ? null : build(superClass);
		List<ClassEntry> interfaceEntries = new ArrayList<>(interfaces.length);
		for (Class<?> implemented : interfaces)
			interfaceEntries.add(build(implemented));
		int modifiers = ref.getModifiers();
		return new BasicClassEntry(className, modifiers, superEntry, interfaceEntries, fields, methods);
	}
}

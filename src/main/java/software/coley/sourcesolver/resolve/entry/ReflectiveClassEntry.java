package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.resolve.generic.GenericType;
import software.coley.sourcesolver.resolve.generic.GenericTypeParameter;
import software.coley.sourcesolver.resolve.generic.GenericTypes;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A class entry implementation that is populated via reflection.
 *
 * @author Matt Coley
 */
public class ReflectiveClassEntry {
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
		Class<?> superClass = cls.isInterface() ? Object.class : cls.getSuperclass();
		Class<?>[] interfaces = cls.getInterfaces();
		ClassEntry superEntry = superClass == null ? null : build(cache, superClass);
		List<ClassEntry> interfaceEntries = new ArrayList<>(interfaces.length);
		for (Class<?> implemented : interfaces)
			interfaceEntries.add(build(cache, implemented));
		Class<?>[] innerClasses = cls.getDeclaredClasses();
		List<ClassEntry> innerClassEntries = new ArrayList<>();
		int modifiers = cls.getModifiers();
		Class<?> outerClass = cls.getDeclaringClass();
		ClassEntry outerClassEntry = outerClass == null ? null : build(cache, outerClass);

		// Placeholder breaks self-referential generic cycles such as Enum<E extends Enum<E>>.
		ClassEntry placeholder = new BasicClassEntry(className, modifiers, superEntry, interfaceEntries,
				innerClassEntries, outerClassEntry, List.of(), null, List.of(), fields, methods);
		cache.put(className, placeholder);

		ClassEntry objectEntry = className.equals("java/lang/Object") ? placeholder : build(cache, Object.class);
		Function<Class<?>, ClassEntry> classProvider = rawClass -> build(cache, rawClass);

		List<GenericTypeParameter> typeParameters = GenericTypes.fromTypeParameters(cls.getTypeParameters(), classProvider, objectEntry);
		GenericType.ClassType genericSuperType = superEntry == null ? null :
				(cls.isInterface() ? GenericTypes.ofClass(superEntry) :
						GenericTypes.fromReflectSuperType(cls.getGenericSuperclass(), classProvider, objectEntry));
		List<GenericType.ClassType> genericInterfaceTypes = GenericTypes.fromReflectInterfaceTypes(cls.getGenericInterfaces(),
				classProvider, objectEntry);

		for (Field field : cls.getDeclaredFields()) {
			String fieldName = field.getName();
			String fieldDescriptor = field.getType().descriptorString();
			int fieldModifiers = field.getModifiers();
			fields.add(new BasicFieldEntry(fieldName, fieldDescriptor, fieldModifiers,
					GenericTypes.fromReflectType(field.getGenericType(), classProvider, objectEntry)));
		}
		for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
			String methodDescriptor = MethodType.methodType(void.class, constructor.getParameterTypes()).descriptorString();
			int methodModifiers = constructor.getModifiers();
			List<GenericType> genericParameterTypes = new ArrayList<>(constructor.getGenericParameterTypes().length);
			for (java.lang.reflect.Type parameterType : constructor.getGenericParameterTypes())
				genericParameterTypes.add(GenericTypes.fromReflectType(parameterType, classProvider, objectEntry));
			methods.add(new BasicMethodEntry("<init>", methodDescriptor, methodModifiers,
					GenericTypes.ofPrimitive(PrimitiveEntry.getPrimitive("V")), genericParameterTypes));
		}
		for (Method method : cls.getDeclaredMethods()) {
			String methodName = method.getName();
			String methodDescriptor = MethodType.methodType(method.getReturnType(), method.getParameterTypes()).descriptorString();
			int methodModifiers = method.getModifiers();
			List<GenericType> genericParameterTypes = new ArrayList<>(method.getGenericParameterTypes().length);
			for (java.lang.reflect.Type parameterType : method.getGenericParameterTypes())
				genericParameterTypes.add(GenericTypes.fromReflectType(parameterType, classProvider, objectEntry));
			methods.add(new BasicMethodEntry(methodName, methodDescriptor, methodModifiers,
					GenericTypes.fromReflectType(method.getGenericReturnType(), classProvider, objectEntry), genericParameterTypes));
		}

		ClassEntry entry = new BasicClassEntry(className, modifiers, superEntry, interfaceEntries,
				innerClassEntries, outerClassEntry, typeParameters, genericSuperType, genericInterfaceTypes, fields, methods);
		cache.put(className, entry);

		// I know this is REALLY cringe putting the inner class population AFTER the building of the model,
		// but if we don't do this we run the risk of running into a cycle.
		for (Class<?> innerClass : innerClasses)
			if (innerClass.getName().startsWith(cls.getName() + "$") && !innerClass.getName().equals(cls.getName()))
				innerClassEntries.add(build(cache, innerClass));

		return entry;
	}
}

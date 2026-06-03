package software.coley.sourcesolver.resolve.generic;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilities for working with the {@link GenericType}.
 *
 * @author Matt Coley
 */
public class GenericTypes {
	private GenericTypes() {}

	/**
	 * @param primitive
	 * 		Primitive being represented.
	 *
	 * @return Generic wrapper for a primitive entry.
	 */
	@Nonnull
	public static GenericType.PrimitiveType ofPrimitive(@Nonnull PrimitiveEntry primitive) {
		return new GenericType.PrimitiveType(primitive);
	}

	/**
	 * @param classEntry
	 * 		The class being represented.
	 *
	 * @return Class type with no explicit type arguments.
	 */
	@Nonnull
	public static GenericType.ClassType ofClass(@Nonnull ClassEntry classEntry) {
		return new GenericType.ClassType(classEntry, List.of());
	}

	/**
	 * @param classEntry
	 * 		The class being represented.
	 * @param typeArguments
	 * 		Type arguments to bind to the class's declared type parameters.
	 * 		If the class has more declared type parameters than provided arguments,
	 * 		the remaining parameters will be left unbound.
	 *
	 * @return Class type with the given bound arguments.
	 */
	@Nonnull
	public static GenericType.ClassType ofClass(@Nonnull ClassEntry classEntry, @Nonnull List<GenericType> typeArguments) {
		return new GenericType.ClassType(classEntry, typeArguments);
	}

	/**
	 * @param genericType
	 * 		Generic type to interpret as a class type.
	 * @param objectEntry
	 * 		Entry for {@code java/lang/Object}, used as a fallback when wildcard bounds are missing or unusable.
	 *
	 * @return Usable class type, or {@code null} if the input is not class-like.
	 */
	@Nullable
	public static GenericType.ClassType asClassType(@Nullable GenericType genericType, @Nonnull ClassEntry objectEntry) {
		GenericType usableType = toUsableType(genericType, objectEntry);
		return usableType instanceof GenericType.ClassType classType ? classType : null;
	}

	/**
	 * Normalizes wildcard types into something member resolution can use directly.
	 *
	 * @param genericType
	 * 		The type to normalize.
	 * @param objectEntry
	 * 		Entry for {@code java/lang/Object}, used as a fallback when wildcard bounds are missing or unusable.
	 *
	 * @return Resolver-usable type, or {@code null} when no type is available.
	 */
	@Nullable
	public static GenericType toUsableType(@Nullable GenericType genericType, @Nonnull ClassEntry objectEntry) {
		if (genericType == null)
			return null;
		if (genericType instanceof GenericType.WildcardType wildcardType) {
			if (wildcardType.lowerBound() != null) {
				DescribableEntry erasure = wildcardType.erasure();
				return erasure instanceof ClassEntry classEntry ? ofClass(classEntry) : ofClass(objectEntry);
			}
			if (wildcardType.upperBound() != null)
				return toUsableType(wildcardType.upperBound(), objectEntry);
			DescribableEntry erasure = wildcardType.erasure();
			return erasure instanceof ClassEntry classEntry ? ofClass(classEntry) : ofClass(objectEntry);
		}
		return genericType;
	}

	/**
	 * @param classType
	 * 		Class type to bind.
	 *
	 * @return Mapping from declared type variables to concrete receiver arguments.
	 */
	@Nonnull
	public static Map<GenericTypeParameter, GenericType> bind(@Nonnull GenericType.ClassType classType) {
		Map<GenericTypeParameter, GenericType> bindings = new LinkedHashMap<>(); // Preserve declaration order for readability.
		List<GenericTypeParameter> parameters = classType.classEntry().getTypeParameters();
		List<GenericType> arguments = classType.typeArguments();
		for (int i = 0; i < Math.min(parameters.size(), arguments.size()); i++)
			bindings.put(parameters.get(i), arguments.get(i));
		return bindings;
	}

	/**
	 * Applies known type-variable bindings to a generic type tree.
	 *
	 * @param genericType
	 * 		Type to substitute into.
	 * @param bindings
	 * 		Mapping from type variables to concrete types.
	 *
	 * @return Substituted type.
	 */
	@Nonnull
	public static GenericType substitute(@Nonnull GenericType genericType,
	                                     @Nonnull Map<GenericTypeParameter, GenericType> bindings) {
		return switch (genericType) {
			case GenericType.PrimitiveType primitiveType -> primitiveType;
			case GenericType.ArrayType arrayType ->
					new GenericType.ArrayType(substitute(arrayType.elementType(), bindings), arrayType.dimensions());
			case GenericType.ClassType classType -> {
				if (classType.typeArguments().isEmpty()) {
					yield classType;
				}
				List<GenericType> substitutedArgs = classType.typeArguments().stream()
						.map(type -> substitute(type, bindings))
						.toList();
				yield new GenericType.ClassType(classType.classEntry(), substitutedArgs);
			}
			case GenericType.TypeVariableType typeVariableType -> {
				GenericType substituted = bindings.get(typeVariableType.parameter());
				yield substituted == null ? typeVariableType : substituted;
			}
			case GenericType.WildcardType wildcardType -> new GenericType.WildcardType(
					wildcardType.upperBound() == null ? null : substitute(wildcardType.upperBound(), bindings),
					wildcardType.lowerBound() == null ? null : substitute(wildcardType.lowerBound(), bindings),
					wildcardType.erasure());
		};
	}

	/**
	 * Re-expresses a resolved receiver as a parent/interface owner while preserving
	 * substituted type arguments.
	 *
	 * @param receiverType
	 * 		Receiver to adapt.
	 * @param ownerEntry
	 * 		Owner to adapt the receiver to.
	 * @param objectEntry
	 * 		Entry for {@code java/lang/Object}, used as a fallback when wildcard bounds are missing or unusable.
	 *
	 * @return Receiver re-bound to the requested owner, or {@code null} if unreachable.
	 */
	@Nullable
	public static GenericType.ClassType adaptToOwner(@Nonnull GenericType.ClassType receiverType,
	                                                 @Nonnull ClassEntry ownerEntry,
	                                                 @Nonnull ClassEntry objectEntry) {
		if (receiverType.classEntry().getName().equals(ownerEntry.getName()))
			return receiverType;

		Map<GenericTypeParameter, GenericType> bindings = bind(receiverType);
		GenericType.ClassType genericSuperType = receiverType.classEntry().getGenericSuperType();
		if (genericSuperType != null) {
			GenericType.ClassType substitutedSuperType = asClassType(substitute(genericSuperType, bindings), objectEntry);
			if (substitutedSuperType != null) {
				GenericType.ClassType match = adaptToOwner(substitutedSuperType, ownerEntry, objectEntry);
				if (match != null)
					return match;
			}
		}
		for (GenericType.ClassType genericInterfaceType : receiverType.classEntry().getGenericInterfaceTypes()) {
			GenericType.ClassType substitutedInterfaceType = asClassType(substitute(genericInterfaceType, bindings), objectEntry);
			if (substitutedInterfaceType != null) {
				GenericType.ClassType match = adaptToOwner(substitutedInterfaceType, ownerEntry, objectEntry);
				if (match != null)
					return match;
			}
		}
		return null;
	}

	/**
	 * @param typeVariables
	 * 		Type variables to convert.
	 * @param classProvider
	 * 		Provider for looking up class entries for reflected types.
	 * @param objectEntry
	 * 		Entry for {@code java/lang/Object}, used as a fallback when wildcard bounds are missing or unusable.
	 *
	 * @return Resolver metadata for reflected type parameters.
	 */
	@Nonnull
	public static List<GenericTypeParameter> fromTypeParameters(@Nonnull TypeVariable<?>[] typeVariables,
	                                                            @Nonnull Function<Class<?>, ClassEntry> classProvider,
	                                                            @Nonnull ClassEntry objectEntry) {
		List<GenericTypeParameter> parameters = new ArrayList<>(typeVariables.length);
		for (TypeVariable<?> typeVariable : typeVariables) {
			DescribableEntry upperBound = erasedType(typeVariable.getBounds().length == 0 ? Object.class : typeVariable.getBounds()[0],
					classProvider, objectEntry);
			parameters.add(new GenericTypeParameter(ownerId(typeVariable.getGenericDeclaration()),
					typeVariable.getName(), upperBound));
		}
		return parameters;
	}

	/**
	 *
	 * @param type
	 * 		Reflected superclass type to convert.
	 * @param classProvider
	 * 		Provider for looking up class entries for reflected types.
	 * @param objectEntry
	 * 		Entry for {@code java/lang/Object}, used as a fallback when wildcard bounds are missing or unusable.
	 *
	 * @return Reflected superclass as a class type, or {@code null}.
	 */
	@Nullable
	public static GenericType.ClassType fromReflectSuperType(@Nullable Type type,
	                                                         @Nonnull Function<Class<?>, ClassEntry> classProvider,
	                                                         @Nonnull ClassEntry objectEntry) {
		if (type == null)
			return null;
		return asClassType(fromReflectType(type, classProvider, objectEntry), objectEntry);
	}

	/**
	 *
	 * @param types
	 * 		Reflected interface types to convert.
	 * @param classProvider
	 * 		Provider for looking up class entries for reflected types.
	 * @param objectEntry
	 * 		Entry for {@code java/lang/Object}, used as a fallback when wildcard bounds are missing or unusable.
	 *
	 * @return Reflected interface types as class types.
	 */
	@Nonnull
	public static List<GenericType.ClassType> fromReflectInterfaceTypes(@Nonnull Type[] types,
	                                                                    @Nonnull Function<Class<?>, ClassEntry> classProvider,
	                                                                    @Nonnull ClassEntry objectEntry) {
		List<GenericType.ClassType> interfaceTypes = new ArrayList<>(types.length);
		for (Type type : types) {
			GenericType.ClassType interfaceType = asClassType(fromReflectType(type, classProvider, objectEntry), objectEntry);
			if (interfaceType != null)
				interfaceTypes.add(interfaceType);
		}
		return interfaceTypes;
	}

	/**
	 * Converts a reflected Java {@link Type} into the resolver's generic type model.
	 *
	 * @param type
	 * 		Reflected type to convert.
	 * @param classProvider
	 * 		Provider for looking up class entries for reflected types.
	 * @param objectEntry
	 * 		Entry for {@code java/lang/Object}, used as a fallback when wildcard bounds are missing or unusable.
	 *
	 * @return Resolver generic type.
	 */
	@Nonnull
	public static GenericType fromReflectType(@Nonnull Type type,
	                                          @Nonnull Function<Class<?>, ClassEntry> classProvider,
	                                          @Nonnull ClassEntry objectEntry) {
		switch (type) {
			case Class<?> rawClass -> {
				return fromClass(rawClass, classProvider);
			}
			case ParameterizedType parameterizedType -> {
				Type rawType = parameterizedType.getRawType();
				if (!(rawType instanceof Class<?> rawClass))
					throw new IllegalStateException("Unsupported parameterized raw type: " + rawType);
				ClassEntry classEntry = getClassEntry(rawClass, classProvider);
				List<GenericType> arguments = new ArrayList<>(parameterizedType.getActualTypeArguments().length);
				for (Type argument : parameterizedType.getActualTypeArguments())
					arguments.add(fromReflectType(argument, classProvider, objectEntry));
				return new GenericType.ClassType(classEntry, arguments);
			}
			case TypeVariable<?> typeVariable -> {
				DescribableEntry upperBound = erasedType(typeVariable.getBounds().length == 0 ? Object.class : typeVariable.getBounds()[0],
						classProvider, objectEntry);
				return new GenericType.TypeVariableType(new GenericTypeParameter(ownerId(typeVariable.getGenericDeclaration()),
						typeVariable.getName(), upperBound));
			}
			case GenericArrayType arrayType -> {
				GenericType componentType = fromReflectType(arrayType.getGenericComponentType(), classProvider, objectEntry);
				if (componentType instanceof GenericType.ArrayType nestedArrayType)
					return new GenericType.ArrayType(nestedArrayType.elementType(), nestedArrayType.dimensions() + 1);
				return new GenericType.ArrayType(componentType, 1);
			}
			case WildcardType wildcardType -> {
				Type[] lowerBounds = wildcardType.getLowerBounds();
				if (lowerBounds.length > 0) {
					return new GenericType.WildcardType(null, fromReflectType(lowerBounds[0], classProvider, objectEntry), objectEntry);
				}
				Type[] upperBounds = wildcardType.getUpperBounds();
				if (upperBounds.length == 0 || upperBounds[0] == Object.class)
					return new GenericType.WildcardType(null, null, objectEntry);
				GenericType upperBound = fromReflectType(upperBounds[0], classProvider, objectEntry);
				return new GenericType.WildcardType(upperBound, null, upperBound.asDescribable());
			}
			default -> throw new IllegalStateException("Unsupported generic reflection type: " + type.getTypeName());
		}
	}

	@Nonnull
	private static GenericType fromClass(@Nonnull Class<?> rawClass, @Nonnull Function<Class<?>, ClassEntry> classProvider) {
		if (rawClass.isPrimitive())
			return new GenericType.PrimitiveType(PrimitiveEntry.getPrimitive(rawClass.descriptorString()));
		if (rawClass.isArray()) {
			int dimensions = 0;
			Class<?> elementType = rawClass;
			while (elementType.isArray()) {
				elementType = elementType.componentType();
				dimensions++;
			}
			return new GenericType.ArrayType(fromClass(elementType, classProvider), dimensions);
		}
		return new GenericType.ClassType(getClassEntry(rawClass, classProvider), List.of());
	}

	@Nonnull
	private static ClassEntry getClassEntry(@Nonnull Class<?> rawClass,
	                                        @Nonnull Function<Class<?>, ClassEntry> classProvider) {
		ClassEntry classEntry = classProvider.apply(rawClass);
		String className = rawClass.getName().replace('.', '/');
		if (classEntry == null)
			throw new IllegalStateException("Missing reflected class entry: " + className);
		return classEntry;
	}

	@Nonnull
	private static DescribableEntry erasedType(@Nonnull Type type,
	                                           @Nonnull Function<Class<?>, ClassEntry> classProvider,
	                                           @Nonnull ClassEntry objectEntry) {
		return switch (type) {
			case Class<?> rawClass -> {
				if (rawClass.isPrimitive())
					yield PrimitiveEntry.getPrimitive(rawClass.descriptorString());
				if (rawClass.isArray()) {
					int dimensions = 0;
					Class<?> elementType = rawClass;
					while (elementType.isArray()) {
						elementType = elementType.componentType();
						dimensions++;
					}
					DescribableEntry elementEntry = erasedType(elementType, classProvider, objectEntry);
					yield elementEntry.toArrayEntry(dimensions);
				}
				yield rawClass == Object.class ? objectEntry : getClassEntry(rawClass, classProvider);
			}
			case ParameterizedType parameterizedType ->
					erasedType(parameterizedType.getRawType(), classProvider, objectEntry);
			case GenericArrayType arrayType ->
					erasedType(arrayType.getGenericComponentType(), classProvider, objectEntry).toArrayEntry(1);
			case TypeVariable<?> typeVariable ->
					erasedType(typeVariable.getBounds().length == 0 ? Object.class : typeVariable.getBounds()[0], classProvider, objectEntry);
			case WildcardType wildcardType -> {
				Type[] lowerBounds = wildcardType.getLowerBounds();
				if (lowerBounds.length > 0)
					yield objectEntry;
				Type[] upperBounds = wildcardType.getUpperBounds();
				yield upperBounds.length == 0 ? objectEntry : erasedType(upperBounds[0], classProvider, objectEntry);
			}
			default -> objectEntry;
		};
	}

	@Nonnull
	private static String ownerId(@Nonnull GenericDeclaration declaration) {
		return switch (declaration) {
			case Class<?> declaringClass -> declaringClass.getName().replace('.', '/');
			case Method method -> {
				String owner = method.getDeclaringClass().getName().replace('.', '/');
				String descriptor = MethodType.methodType(method.getReturnType(), method.getParameterTypes()).descriptorString();
				yield owner + "#" + method.getName() + descriptor;
			}
			case Constructor<?> constructor -> {
				String owner = constructor.getDeclaringClass().getName().replace('.', '/');
				String descriptor = MethodType.methodType(void.class, constructor.getParameterTypes()).descriptorString();
				yield owner + "#<init>" + descriptor;
			}
			default -> declaration.toString();
		};
	}
}

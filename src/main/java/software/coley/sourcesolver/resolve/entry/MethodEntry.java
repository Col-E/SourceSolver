package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata model for a method declaration.
 *
 * @author Matt Coley
 */
public non-sealed interface MethodEntry extends MemberEntry {
	/**
	 * @return Descriptor of the method's return type.
	 */
	@Nonnull
	default String getReturnDescriptor() {
		String methodDescriptor = getDescriptor();
		return methodDescriptor.substring(methodDescriptor.lastIndexOf(')') + 1);
	}

	/**
	 * @return List of descriptors of all the method's parameters.
	 */
	@Nonnull
	default List<String> getParameterDescriptors() {
		List<String> parameterDescriptors = new ArrayList<>();
		String methodDescriptor = getDescriptor();
		int currentOffset = 1;
		while (methodDescriptor.charAt(currentOffset) != ')') {
			final int currentArgumentTypeOffset = currentOffset;
			while (methodDescriptor.charAt(currentOffset) == '[')
				currentOffset++;
			if (methodDescriptor.charAt(currentOffset++) == 'L') {
				int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
				currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
			}
			String parameterDescriptor = methodDescriptor.substring(currentArgumentTypeOffset, currentOffset);
			parameterDescriptors.add(parameterDescriptor);
		}
		return parameterDescriptors;
	}

	@Override
	default boolean isField() {
		return false;
	}

	@Override
	default boolean isMethod() {
		return true;
	}

	default boolean isVarargs() {
		return (getAccess() & Modifier.TRANSIENT /* same modifier mask as varargs */) != 0;
	}
}

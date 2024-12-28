package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;

/**
 * Metadata model for common aspects of fields and methods.
 *
 * @author Matt Coley
 */
public sealed interface MemberEntry extends AccessedEntry, DescribableEntry permits FieldEntry, MethodEntry {
	/**
	 * @return {@code true} when this entry represents a field.
	 */
	boolean isField();

	/**
	 * @return {@code true} when this entry represents a method.
	 */
	boolean isMethod();

	/**
	 * @return Member name.
	 */
	@Nonnull
	String getName();

	@Override
	default boolean isAssignableFrom(@Nonnull DescribableEntry other) {
		if (other instanceof MemberEntry otherMember)
			return getName().equals(otherMember.getName()) && getDescriptor().equals(otherMember.getDescriptor());
		return false;
	}
}

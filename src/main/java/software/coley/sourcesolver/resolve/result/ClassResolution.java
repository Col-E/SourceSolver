package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.MemberEntry;

import static software.coley.sourcesolver.resolve.result.Resolutions.ofMember;
import static software.coley.sourcesolver.resolve.result.Resolutions.unknown;

/**
 * Resolution of a class.
 *
 * @author Matt Coley
 */
non-sealed public interface ClassResolution extends DescribableResolution {
	/**
	 * @return The resolved class type.
	 */
	@Nonnull
	ClassEntry getClassEntry();

	@Nonnull
	@Override
	default DescribableEntry getDescribableEntry() {
		return getClassEntry();
	}

	/**
	 * @param member
	 * 		Declared member to look up.
	 *
	 * @return Resolution of the declared member in this resolution's class entry.
	 */
	@Nonnull
	default Resolution getDeclaredMemberResolution(@Nonnull MemberEntry member) {
		return getDeclaredMemberResolution(member.getName(), member.getDescriptor());
	}

	/**
	 * @param name
	 * 		Declared member name to look up.
	 * @param desc
	 * 		Declared member descriptor to look up.
	 *
	 * @return Resolution of the declared member in this resolution's class entry.
	 */
	@Nonnull
	default Resolution getDeclaredMemberResolution(@Nonnull String name, @Nonnull String desc) {
		ClassEntry classEntry = getClassEntry();
		MemberEntry member = classEntry.getDeclaredField(name, desc);
		if (member != null)
			return ofMember(classEntry, member);
		member = classEntry.getDeclaredMethod(name, desc);
		if (member != null)
			return ofMember(classEntry, member);
		return unknown();
	}
}

package software.coley.sourcesolver.resolve.result;

import jakarta.annotation.Nonnull;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;

import java.util.List;
import java.util.Objects;

/**
 * Resolution of multiple members.
 * Used for static imports.
 *
 * @author Matt Coley
 */
non-sealed public interface MultiMemberResolution extends Resolution {
	/**
	 * @return List of the resolved members.
	 */
	@Nonnull
	List<ClassMemberPair> getMemberEntries();

	@Override
	default boolean matches(@Nonnull Resolution other) {
		return other instanceof MultiMemberResolution otherMultiMember &&
				Objects.equals(getMemberEntries().stream().map(p -> p.ownerEntry().getName() + '.' + p.memberEntry().getName() + '.' + p.memberEntry().getDescriptor()).toList(),
						otherMultiMember.getMemberEntries().stream().map(p -> p.ownerEntry().getName() + '.' + p.memberEntry().getName() + '.' + p.memberEntry().getDescriptor()).toList());
	}
}

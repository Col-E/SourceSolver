package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public record ClassMemberPair(@Nonnull ClassEntry ownerEntry, @Nonnull MemberEntry memberEntry) {}

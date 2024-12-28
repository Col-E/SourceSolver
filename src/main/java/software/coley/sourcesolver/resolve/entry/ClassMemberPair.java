package software.coley.sourcesolver.resolve.entry;

import jakarta.annotation.Nonnull;

public record ClassMemberPair(@Nonnull ClassEntry ownerEntry, @Nonnull MemberEntry memberEntry) {}

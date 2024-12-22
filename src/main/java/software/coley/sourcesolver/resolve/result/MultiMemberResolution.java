package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;
import software.coley.sourcesolver.resolve.entry.MemberEntry;

import javax.annotation.Nonnull;
import java.util.List;

non-sealed public interface MultiMemberResolution extends Resolution {
	@Nonnull
	List<ClassMemberPair> getMemberEntries();
}

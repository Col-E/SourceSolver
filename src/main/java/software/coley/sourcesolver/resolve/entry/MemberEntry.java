package software.coley.sourcesolver.resolve.entry;

import javax.annotation.Nonnull;

public interface MemberEntry extends AccessedEntry, DescribableEntry {
	boolean isField();

	boolean isMethod();

	@Nonnull
	String getName();
}

package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;

public interface Ranged {
	@Nonnull
	Range getRange();
}

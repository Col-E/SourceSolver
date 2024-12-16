package software.coley.sourcesolver.resolve.result;

import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;

import javax.annotation.Nonnull;
import java.util.List;

non-sealed public interface MultiClassResolution extends Resolution {
	@Nonnull
	List<ClassEntry> getClassEntries();
}
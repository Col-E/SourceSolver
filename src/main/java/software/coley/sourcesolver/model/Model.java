package software.coley.sourcesolver.model;

import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.result.Resolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Model extends Ranged {
	@Nonnull
	Resolution resolve(@Nonnull Resolver resolver);

	@Nonnull
	Resolution resolveAt(@Nonnull Resolver resolver, int index);

	@Nullable
	Model getChildAtPosition(int position);

	@Nonnull
	String getSource(@Nonnull CompilationUnitModel unit);

	@Nonnull
	List<Model> getChildren();

	@Nullable
	Model getParent();
}

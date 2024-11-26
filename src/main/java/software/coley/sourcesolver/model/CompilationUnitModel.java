package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

public class CompilationUnitModel extends AbstractModel {
	private final List<ClassModel> declaredClasses;

	public CompilationUnitModel(@Nonnull Range range, @Nonnull List<ClassModel> declaredClasses) {
		super(range, declaredClasses);
		this.declaredClasses = declaredClasses;
	}

	public List<ClassModel> getDeclaredClasses() {
		return declaredClasses;
	}
}

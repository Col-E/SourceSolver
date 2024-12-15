package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

public class CompilationUnitModel extends AbstractModel {
	private final List<ClassModel> declaredClasses;
	private final String inputSource;

	public CompilationUnitModel(@Nonnull Range range, @Nonnull String inputSource, @Nonnull List<ClassModel> declaredClasses) {
		super(range, declaredClasses);
		this.inputSource = inputSource;
		this.declaredClasses = declaredClasses;
	}

	/**
	 * @return Original source code used to construct this unit.
	 */
	@Nonnull
	public String getInputSource() {
		return inputSource;
	}

	@Nonnull
	public List<ClassModel> getDeclaredClasses() {
		return declaredClasses;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CompilationUnitModel that = (CompilationUnitModel) o;

		return declaredClasses.equals(that.declaredClasses);
	}

	@Override
	public int hashCode() {
		return declaredClasses.hashCode();
	}
}

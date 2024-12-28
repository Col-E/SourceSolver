package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;

import static software.coley.sourcesolver.model.ChildSupplier.of;

public class DeconstructionPatternModel extends AbstractPatternModel {
	private final AbstractExpressionModel deconstructor;
	private final List<AbstractPatternModel> nestedPatterns;


	public DeconstructionPatternModel(@Nonnull Range range, @Nonnull AbstractExpressionModel deconstructor,
	                                  @Nonnull List<AbstractPatternModel> nestedPatterns) {
		super(range, of(deconstructor), of(nestedPatterns));
		this.deconstructor = deconstructor;
		this.nestedPatterns = nestedPatterns;
	}

	@Nonnull
	public AbstractExpressionModel getDeconstructor() {
		return deconstructor;
	}

	@Nonnull
	public List<AbstractPatternModel> getNestedPatterns() {
		return nestedPatterns;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DeconstructionPatternModel that = (DeconstructionPatternModel) o;

		if (!deconstructor.equals(that.deconstructor)) return false;
		return nestedPatterns.equals(that.nestedPatterns);
	}

	@Override
	public int hashCode() {
		int result = deconstructor.hashCode();
		result = 31 * result + nestedPatterns.hashCode();
		return result;
	}
}

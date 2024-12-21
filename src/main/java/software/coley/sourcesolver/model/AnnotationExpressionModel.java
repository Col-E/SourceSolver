package software.coley.sourcesolver.model;

import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationExpressionModel extends AbstractExpressionModel implements NameHolder {
	private final NameExpressionModel name;
	private final List<AnnotationArgumentModel> arguments;

	public AnnotationExpressionModel(@Nonnull Range range, @Nonnull NameExpressionModel name,
	                                 @Nonnull List<AnnotationArgumentModel> arguments) {
		super(range);
		this.name = name;
		this.arguments = arguments;
	}

	@Nonnull
	@Override
	public NameExpressionModel getNameModel() {
		return name;
	}

	@Nonnull
	public List<AnnotationArgumentModel> getArguments() {
		return arguments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AnnotationExpressionModel that = (AnnotationExpressionModel) o;

		if (!name.equals(that.name)) return false;
		return arguments.equals(that.arguments);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + arguments.hashCode();
		return result;
	}

	@Override
	public String toString() {
		String display = "@" + name;
		if (!arguments.isEmpty()) {
			display += "(" + arguments.stream()
					.map(AnnotationArgumentModel::toString)
					.collect(Collectors.joining(", ")) +
					")";

		}
		return display;
	}
}

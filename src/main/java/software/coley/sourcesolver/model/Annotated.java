package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;
import java.util.List;

public interface Annotated {
	@Nonnull
	List<AnnotationExpressionModel> getAnnotationModels();
}

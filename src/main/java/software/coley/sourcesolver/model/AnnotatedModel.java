package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;
import java.util.List;

public interface AnnotatedModel extends Model {
	@Nonnull
	List<AnnotationExpressionModel> getAnnotations();
}

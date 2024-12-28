package software.coley.sourcesolver.model;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * A model that can be annotated.
 *
 * @author Matt Coley
 */
public interface AnnotatedModel extends Model {
	/**
	 * @return Annotations on this model.
	 */
	@Nonnull
	List<AnnotationExpressionModel> getAnnotations();
}

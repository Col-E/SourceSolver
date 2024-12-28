package software.coley.sourcesolver.model;

import javax.annotation.Nonnull;

/**
 * Visitor for {@link Model} that walks down {@link Model#getChildren()}.
 *
 * @author Matt Coley
 */
public interface ModelVisitor {
	/**
	 * @param model
	 * 		Model visited.
	 *
	 * @return {@code true} to continue visiting down the model tree <i>(children)</i>.
	 * {@code false} to abort the visitation.
	 */
	boolean visit(@Nonnull Model model);
}

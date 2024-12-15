package software.coley.sourcesolver.mapping;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.AbstractModel;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TypeArgumentsMapper implements Mapper<TypeArgumentsMapper.Args, TypeArgumentsMapper.ArgsTree> {
	@Nonnull
	@Override
	public Args map(@Nonnull MappingContext context, @Nonnull EndPosTable table, @Nullable ArgsTree tree) {
		List<AbstractModel> typeArguments = tree == null || tree.getTypeArguments() == null ? Collections.emptyList() :
				tree.getTypeArguments().stream().map(t -> {
					if (t instanceof ExpressionTree e)
						return context.map(ExpressionMapper.class, e);
					else if (t instanceof StatementTree s)
						return context.map(StatementMapper.class, s);
					else
						throw new IllegalStateException("Unsupported type argument AST node: " + t.getClass().getSimpleName());
				}).toList();
		return new Args(typeArguments);
	}

	public interface ArgsTree extends Tree {
		List<? extends Tree> getTypeArguments();

		@Override
		default Kind getKind() {
			return Kind.OTHER;
		}

		@Override
		default <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
			return null;
		}
	}

	/**
	 * Intermediate holder for the type arguments.
	 */
	public static class Args extends AbstractModel {
		private final List<AbstractModel> arguments;

		public Args(@Nonnull List<AbstractModel> arguments) {
			super(Range.UNKNOWN);
			this.arguments = arguments;
		}

		@Nonnull
		public List<AbstractModel> getArguments() {
			return arguments;
		}

		@Override
		public boolean equals(Object o) {
			return false;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}
}

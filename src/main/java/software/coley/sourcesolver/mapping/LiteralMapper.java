package software.coley.sourcesolver.mapping;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.LiteralModel;

import javax.annotation.Nonnull;

import java.util.Objects;

import static software.coley.sourcesolver.util.Range.extractRange;

public class LiteralMapper {
	@Nonnull
	public LiteralModel map(@Nonnull EndPosTable table, @Nonnull LiteralTree tree) {
		Object content = tree.getValue();
		var kind = switch (tree.getKind()) {
			case INT_LITERAL ->  LiteralModel.Kind.INT;
			case LONG_LITERAL ->  LiteralModel.Kind.LONG;
			case FLOAT_LITERAL ->  LiteralModel.Kind.FLOAT;
			case DOUBLE_LITERAL ->  LiteralModel.Kind.DOUBLE;
			case BOOLEAN_LITERAL ->  LiteralModel.Kind.BOOLEAN;
			case CHAR_LITERAL ->  LiteralModel.Kind.CHAR;
			case STRING_LITERAL ->  LiteralModel.Kind.STRING;
			case NULL_LITERAL ->  LiteralModel.Kind.NULL;
			default -> LiteralModel.Kind.ERROR;
		};
		return new LiteralModel(extractRange(table, tree), kind, content);
	}
}

package software.coley.sourcesolver;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import software.coley.sourcesolver.mapping.CompilationUnitMapper;
import software.coley.sourcesolver.model.CompilationUnitModel;

import javax.annotation.Nonnull;
import javax.tools.JavaFileManager;
import java.lang.reflect.Field;

public class Parser {
	private Context context;
	private ParserFactory factory;

	public Parser() {
		context = new Context();
		context.put(JavaFileManager.class, new NoopFileManager());
		context.put(Log.logKey, new ErrorIgnoringLog(context));
		regenerateFactory();
	}

	@SuppressWarnings("ConstantValue")
	public void setContext(@Nonnull Context context) {
		if (context == null)
			throw new IllegalArgumentException("Cannot assign a 'null' context!");
		this.context = context;
		regenerateFactory();
	}

	public <T> T getContextProperty(Class<T> key) {
		return context.get(key);
	}

	public <T> void putContextProperty(Class<T> key, T value) {
		context.put(key, value);
	}

	public <T> void putContextProperty(Class<T> key, Context.Factory<T> factory) {
		context.put(key, factory);
	}

	protected void regenerateFactory() {
		factory = ParserFactory.instance(context);
	}

	@Nonnull
	@SuppressWarnings("ConstantValue")
	public CompilationUnitModel parse(@Nonnull String source) {
		// Throw if no source is provided
		if (source == null)
			throw new IllegalArgumentException("Must provide source to parse");

		// Build parser for source and extract the end-pos table so we can resolve AST ranges
		JavacParser parser = factory.newParser(source,
				false /* keepJavadoc */,
				true  /* keepEndPos */,
				false /* keepLineMap */
		);
		EndPosTable table;
		try {
			table = extractEndPosTable(parser);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to extract end-pos table from javac parser", ex);
		}

		// Parse the compilation unit and convert to our own lightweight model
		CompilationUnitTree unit = parser.parseCompilationUnit();
		return mapCompilationUnit(table, unit);
	}

	@Nonnull
	protected CompilationUnitModel mapCompilationUnit(@Nonnull EndPosTable table, @Nonnull CompilationUnitTree unit) {
		return new CompilationUnitMapper().map(table, unit);
	}

	@Nonnull
	protected EndPosTable extractEndPosTable(@Nonnull JavacParser parser) throws Exception {
		Field field = JavacParser.class.getDeclaredField("endPosTable");
		field.setAccessible(true);
		return (EndPosTable) field.get(parser);
	}
}

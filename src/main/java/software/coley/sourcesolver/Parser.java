package software.coley.sourcesolver;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.mapping.CompilationUnitMapper;
import software.coley.sourcesolver.mapping.MappingContext;
import software.coley.sourcesolver.mapping.MappingContextProvider;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.util.RangeExtractor;
import software.coley.sourcesolver.util.compat.FactoryFactory;
import software.coley.sourcesolver.util.compat.RangeExtractorFactory;

import javax.tools.JavaFileManager;

/**
 * Initiates the parsing of source code and provides {@link CompilationUnitModel} in return.
 *
 * @author Matt Coley
 */
public class Parser {
	private Context context;
	private ParserFactory factory;
	private MappingContextProvider mappingContextFactory = MappingContext::new;

	/**
	 * New parser.
	 */
	public Parser() {
		context = new Context();
		context.put(JavaFileManager.class, new NoopFileManager());
		context.put(Log.logKey, new ErrorIgnoringLog(context));
		regenerateFactory();
	}

	/**
	 * Allows overriding {@link MappingContext} behavior by providing a factory that can create user-defined subclasses.
	 *
	 * @param mappingContextFactory
	 * 		New factory to produce {@link MappingContext} instances.
	 */
	public void setMappingContextFactory(@Nonnull MappingContextProvider mappingContextFactory) {
		this.mappingContextFactory = mappingContextFactory;
	}

	/**
	 * Allows controlling lower level javac aspects by providing a full context.
	 *
	 * @param context
	 * 		New javac context object.
	 */
	@SuppressWarnings("ConstantValue")
	public void setJavacContext(@Nonnull Context context) {
		if (context == null)
			throw new IllegalArgumentException("Cannot assign a 'null' context!");
		this.context = context;
		regenerateFactory();
	}

	/**
	 * Get a value in the javac context.
	 *
	 * @param key
	 * 		Javac context key.
	 * @param <T>
	 * 		Content value type.
	 *
	 * @return Javac context value.
	 */
	@Nullable
	public <T> T getContextProperty(@Nonnull Class<T> key) {
		return context.get(key);
	}

	/**
	 * Set a value in the javac context.
	 *
	 * @param key
	 * 		Javac context key.
	 * @param value
	 * 		Value to assign.
	 * @param <T>
	 * 		Content value type.
	 */
	public <T> void putContextProperty(@Nonnull Class<T> key, @Nonnull T value) {
		context.put(key, value);
	}

	/**
	 * Set a value in the javac context.
	 *
	 * @param key
	 * 		Javac context key.
	 * @param factory
	 * 		Value factory to assign.
	 * @param <T>
	 * 		Content value type.
	 */
	public <T> void putContextProperty(@Nonnull Class<T> key, @Nonnull Context.Factory<T> factory) {
		context.put(key, factory);
	}

	/**
	 * Regenerates the javac parser factory.
	 */
	protected void regenerateFactory() {
		factory = ParserFactory.instance(context);
	}

	/**
	 * Maps Java source code to our compilation unit model.
	 *
	 * @param source
	 * 		Java source code.
	 *
	 * @return Parsed model.
	 *
	 * @throws IllegalArgumentException
	 * 		When the source is null, or if a critical error occurs.
	 */
	@Nonnull
	@SuppressWarnings("ConstantValue")
	public CompilationUnitModel parse(@Nonnull String source) {
		// Throw if no source is provided
		if (source == null)
			throw new IllegalArgumentException("Must provide source to parse");

		// Build parser for source and extract the end-pos table so that we can resolve AST ranges
		JavacParser parser = factory.newParser(source,
				false /* keepJavadoc */,
				true  /* keepEndPos */,
				false /* keepLineMap */
		);

		// Parse the compilation unit and convert to our own lightweight model
		CompilationUnitTree unit = parser.parseCompilationUnit();
		RangeExtractor extractor = FactoryFactory.getRangeExtractor().apply(parser);
		return mapCompilationUnit(source, extractor, unit);
	}

	/**
	 * Maps the javac model to our own.
	 *
	 * @param source
	 * 		Java source code.
	 * @param extractor
	 * 		Range extractor to use for calculating source code ranges of trees.
	 * @param unit
	 * 		Root tree element.
	 *
	 * @return Parsed model.
	 */
	@Nonnull
	protected CompilationUnitModel mapCompilationUnit(@Nonnull String source, @Nonnull RangeExtractor extractor, @Nonnull CompilationUnitTree unit) {
		MappingContext mappingContext = mappingContextFactory.newMappingContext(extractor, source);
		mappingContext.setMapperSupplier(CompilationUnitMapper.class, () -> new CompilationUnitMapper(source));
		return mappingContext.map(CompilationUnitMapper.class, unit);
	}

}

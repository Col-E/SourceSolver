package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.sourcesolver.model.Model;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Centralized mapping control.
 *
 * @author Matt Coley
 */
public class MappingContext {
	private static final Map<Class<?>, Supplier<Mapper<?, ?>>> mapperSuppliersByClass = new IdentityHashMap<>();
	private final EndPosTable table;
	private final String source;
	private String className;

	/**
	 * @param table
	 * 		Table to lookup tree positions within.
	 * @param source
	 * 		Original source code being parsed.
	 */
	public MappingContext(@Nonnull EndPosTable table, @Nonnull String source) {
		this.table = table;
		this.source = source;

		initializeDefaultMappers();
	}

	/**
	 * @return Original source code being parsed.
	 */
	@Nonnull
	public String getSource() {
		return source;
	}

	/**
	 * @return Table to lookup tree positions within.
	 */
	@Nonnull
	public EndPosTable getTable() {
		return table;
	}

	/**
	 * @return Name of class being mapped.
	 */
	@Nullable
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 * 		Name of class mapped.
	 */
	public void setClassName(@Nonnull String className) {
		this.className = className;
	}

	/**
	 * @param mapperType
	 * 		Mapper class.
	 * @param mapperImplementation
	 * 		Implementation of the class.
	 * @param <T>
	 * 		Mapper type.
	 */
	public <T extends Mapper<?, ?>> void setMapper(@Nonnull Class<T> mapperType, @Nonnull T mapperImplementation) {
		setMapperSupplier(mapperType, () -> mapperImplementation);
	}

	/**
	 * @param mapperType
	 * 		Mapper class.
	 * @param mapperSupplier
	 * 		Supplier to provide an implementation of the class.
	 * @param <T>
	 * 		Mapper type.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Mapper<?, ?>> void setMapperSupplier(@Nonnull Class<T> mapperType, @Nonnull Supplier<T> mapperSupplier) {
		mapperSuppliersByClass.put(mapperType, (Supplier<Mapper<?, ?>>) mapperSupplier);
	}

	/**
	 * @param mapperType
	 * 		Mapper class.
	 * @param <T>
	 * 		Mapper type.
	 *
	 * @return Implementation of the mapper class.
	 *
	 * @throws IllegalStateException
	 * 		When no such implementation exists for the given mapper type.
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> T getMapper(Class<T> mapperType) {
		Supplier<Mapper<?, ?>> mapperSupplier = mapperSuppliersByClass.get(mapperType);
		if (mapperSupplier == null)
			throw new IllegalStateException("Unknown mapper type requested: " + mapperType);
		Mapper<?, ?> mapper = mapperSupplier.get();
		if (mapper == null)
			throw new IllegalStateException("Mapper supplier for type was null: " + mapperType);
		return (T) mapper;
	}

	/**
	 * @param mapperType
	 * 		Mapper class.
	 * @param tree
	 * 		Tree to map.
	 * @param <M>
	 * 		Model type to output.
	 * @param <T>
	 * 		Tree type to convert.
	 * @param <X>
	 * 		Mapper type to handle conversion.
	 *
	 * @return Model representation of the tree.
	 */
	@Nonnull
	@SuppressWarnings("ConstantValue")
	public <M extends Model, T extends Tree, X extends Mapper<M, T>> M map(@Nonnull Class<X> mapperType, @Nonnull T tree) {
		if (tree == null)
			throw new IllegalStateException("Cannot map 'null' tree value to type " + mapperType.getSimpleName());
		return getMapper(mapperType).map(this, table, tree);
	}

	/**
	 * @param mapperType
	 * 		Mapper class.
	 * @param tree
	 * 		Tree to map.
	 * @param defaultValueSupplier
	 * 		Supplier to provide a fallback model.
	 * @param <M>
	 * 		Model type to output.
	 * @param <T>
	 * 		Tree type to convert.
	 * @param <X>
	 * 		Mapper type to handle conversion.
	 *
	 * @return Model representation of the tree.
	 */
	@Nonnull
	public <M extends Model, T extends Tree, X extends Mapper<M, T>> M mapOr(@Nonnull Class<X> mapperType, @Nullable T tree,
	                                                                         @Nonnull Supplier<M> defaultValueSupplier) {
		if (tree == null)
			return defaultValueSupplier.get();
		return getMapper(mapperType).map(this, table, tree);
	}

	/**
	 * Setup mappers.
	 */
	private void initializeDefaultMappers() {
		setMapper(AnnotationUseMapper.class, new AnnotationUseMapper());
		setMapper(ArrayDeclarationMapper.class, new ArrayDeclarationMapper());
		setMapper(BinaryMapper.class, new BinaryMapper());
		setMapper(BlockMapper.class, new BlockMapper());
		setMapper(CaseLabelMapper.class, new CaseLabelMapper());
		setMapper(CaseMapper.class, new CaseMapper());
		setMapper(CastMapper.class, new CastMapper());
		setMapper(CatchMapper.class, new CatchMapper());
		setMapper(ClassMapper.class, new ClassMapper());
		// setMapper(CompilationUnitMapper.class, new CompilationUnitMapper()); // Entry point, set externally
		setMapper(ExpressionMapper.class, new ExpressionMapper());
		setMapper(IdentifierMapper.class, new IdentifierMapper());
		setMapper(ImportMapper.class, new ImportMapper());
		setMapper(InstanceofMapper.class, new InstanceofMapper());
		setMapper(LambdaMapper.class, new LambdaMapper());
		setMapper(LiteralMapper.class, new LiteralMapper());
		setMapper(MemberReferenceMapper.class, new MemberReferenceMapper());
		setMapper(MemberSelectMapper.class, new MemberSelectMapper());
		setMapper(MethodBodyMapper.class, new MethodBodyMapper());
		setMapper(MethodInvocationMapper.class, new MethodInvocationMapper());
		setMapper(MethodMapper.class, new MethodMapper());
		setMapper(ModifiersMapper.class, new ModifiersMapper());
		setMapper(NameMapper.class, new NameMapper());
		setMapper(NewClassMapper.class, new NewClassMapper());
		// setMapper(PackageMapper.class, new PackageMapper()); // Set externally
		setMapper(PatternMapper.class, new PatternMapper());
		setMapper(StatementMapper.class, new StatementMapper());
		setMapper(StaticInitializerMethodMapper.class, new StaticInitializerMethodMapper());
		setMapper(TryMapper.class, new TryMapper());
		setMapper(TypeMapper.class, new TypeMapper());
		setMapper(TypeArgumentsMapper.class, new TypeArgumentsMapper());
		setMapper(TypeParameterMapper.class, new TypeParameterMapper());
		setMapper(UnaryMapper.class, new UnaryMapper());
		setMapper(VariableMapper.class, new VariableMapper());
	}
}

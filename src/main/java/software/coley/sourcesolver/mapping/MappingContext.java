package software.coley.sourcesolver.mapping;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MappingContext {
	private static final Map<Class<?>, Supplier<Mapper<?, ?>>> mapperSuppliersByClass = new IdentityHashMap<>();
	private final EndPosTable table;
	private final String source;

	public MappingContext(@Nonnull EndPosTable table, @Nonnull String source) {
		this.table = table;
		this.source = source;

		initializeDefaultMappers();
	}

	@Nonnull
	public String getSource() {
		return source;
	}

	@Nonnull
	public EndPosTable getTable() {
		return table;
	}

	@Nonnull
	public Range range(@Nonnull Tree tree) {
		return Range.extractRange(table, tree);
	}

	public <T extends Mapper<?, ?>> void setMapper(@Nonnull Class<T> mapperType, @Nonnull T mapperImplementation) {
		setMapperSupplier(mapperType, () -> mapperImplementation);
	}

	@SuppressWarnings("unchecked")
	public <T extends Mapper<?, ?>> void setMapperSupplier(@Nonnull Class<T> mapperType, @Nonnull Supplier<T> mapperSupplier) {
		mapperSuppliersByClass.put(mapperType, (Supplier<Mapper<?, ?>>) mapperSupplier);
	}

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

	@Nonnull
	@SuppressWarnings("ConstantValue")
	public <M extends Model, T extends Tree, X extends Mapper<M, T>> M map(@Nonnull Class<X> mapperType, @Nonnull T tree) {
		if (tree == null)
			throw new IllegalStateException("Cannot map 'null' tree value to type " + mapperType.getSimpleName());
		return getMapper(mapperType).map(this, table, tree);
	}

	@Nonnull
	public <M extends Model, T extends Tree, X extends Mapper<M, T>> M mapOr(@Nonnull Class<X> mapperType, @Nullable T tree,
	                                                                         @Nonnull Supplier<M> defaultValueSupplier) {
		if (tree == null)
			return defaultValueSupplier.get();
		return getMapper(mapperType).map(this, table, tree);
	}

	private void initializeDefaultMappers() {
		setMapper(AnnotationUseMapper.class, new AnnotationUseMapper());
		setMapper(ArrayDeclarationMapper.class, new ArrayDeclarationMapper());
		setMapper(BinaryMapper.class, new BinaryMapper());
		setMapper(BlockMapper.class, new BlockMapper());
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

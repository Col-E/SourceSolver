package software.coley.sourcesolver.util.compat;

import com.sun.tools.javac.parser.JavacParser;
import software.coley.sourcesolver.util.RangeExtractor;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

/**
 * Fuckass factory to get factories.
 */
public class FactoryFactory {
	private static final int JAVA_VERSION = Runtime.version().feature();

	@SuppressWarnings("unchecked")
	public static Function<JavacParser, RangeExtractor> getRangeExtractor() {
		String className = JAVA_VERSION >= 27
				? FactoryFactory.class.getPackageName() + "." + "RangeExtractorFactory27"
				: FactoryFactory.class.getPackageName() + "." + "RangeExtractorFactory";
		try {
			Class<?> clazz = Class.forName(className);
			MethodHandle mh = MethodHandles.lookup().findStatic(
					clazz,
					"get",
					MethodType.methodType(RangeExtractor.class, JavacParser.class) // return + param
			);
			return (Function<JavacParser, RangeExtractor>) LambdaMetafactory.metafactory(
					MethodHandles.lookup(),
					"apply",                                           // name in functional interface
					MethodType.methodType(Function.class),             // factory type
					MethodType.methodType(Object.class, Object.class), // erased signature
					mh,                                                // target method handle
					MethodType.methodType(RangeExtractor.class, JavacParser.class) // exact signature
			).getTarget().invokeExact();

		} catch (Throwable t) {
			throw new IllegalStateException("Failed to select factory function for 'RangeExtractor'", t);
		}
	}
}

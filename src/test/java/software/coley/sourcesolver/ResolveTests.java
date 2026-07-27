package software.coley.sourcesolver;

import org.junit.jupiter.api.Test;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.model.MemberSelectExpressionModel;
import software.coley.sourcesolver.model.Model;
import software.coley.sourcesolver.model.ScopeLookup;
import software.coley.sourcesolver.model.VariableModel;
import software.coley.sourcesolver.resolve.BasicResolver;
import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;
import software.coley.sourcesolver.resolve.entry.DescribableEntry;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.BasicClassEntry;
import software.coley.sourcesolver.resolve.entry.BasicMethodEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.entry.PrimitiveEntry;
import software.coley.sourcesolver.resolve.generic.GenericTypes;
import software.coley.sourcesolver.resolve.result.ArrayResolution;
import software.coley.sourcesolver.resolve.result.ClassResolution;
import software.coley.sourcesolver.resolve.result.DescribableResolution;
import software.coley.sourcesolver.resolve.result.FieldResolution;
import software.coley.sourcesolver.resolve.result.MethodResolution;
import software.coley.sourcesolver.resolve.result.MultiMemberResolution;
import software.coley.sourcesolver.resolve.result.PackageResolution;
import software.coley.sourcesolver.resolve.result.PrimitiveResolution;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.resolve.result.Resolutions;
import software.coley.sourcesolver.resolve.result.VariableResolution;
import software.coley.sourcesolver.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.reflect.Modifier.PUBLIC;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SameParameterValue")
public class ResolveTests {
	private static final String CLASS_CHAR_SEQ_MAPPER = "sample/CharSeqMapper";
	private static final String CLASS_FIXED_DATA_LIST = "sample/ExampleFixedList";
	private static final String CLASS_FIXED_DATA_PROCESSOR = "sample/FixedDataProcessor";
	private static final String CLASS_OPTION_PANES = "sample/OptionPanes";
	private static final String CLASS_STRING = "java/lang/String";
	private static final String CLASS_MATH = "java/lang/Math";

	private static final EntryPool pool = Utils.getSharedPool();
	private static final Parser parser = new Parser();

	@Test
	void testExampleFixedList() {
		String sourceCode = readSrc(CLASS_FIXED_DATA_LIST);
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "ExampleFixedList<T>"),
				CLASS_FIXED_DATA_LIST);
		assertClassResolution(resolutionAtStart(resolver, sourceCode, "public class"),
				CLASS_FIXED_DATA_LIST);
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "AbstractList<T>"),
				"java/util/AbstractList");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "implements List<T> {", "implements L".length()),
				"java/util/List");
	}

	@Test
	void testFixedDataProcessorCases() {
		String sourceCode = readSrc(CLASS_FIXED_DATA_PROCESSOR);
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertPackageResolution(resolutionAtOffset(resolver, sourceCode, "package sample;", "package s".length()),
				"sample");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "import java.nio.file.Path;"),
				"java/nio/file/Path");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "class FixedDataProcessor {"),
				CLASS_FIXED_DATA_PROCESSOR);
		assertFieldResolution(resolutionAtMiddle(resolver, sourceCode, "String DATA_SPLIT = "),
				CLASS_FIXED_DATA_PROCESSOR, "DATA_SPLIT", "Ljava/lang/String;");
		assertFieldResolution(resolutionAtMiddle(resolver, sourceCode, "split(DATA_SPLIT)"),
				CLASS_FIXED_DATA_PROCESSOR, "DATA_SPLIT", "Ljava/lang/String;");
		assertFieldResolution(resolutionAtMiddle(resolver, sourceCode, "int PRIMARY_ITEM_SHIFT = "),
				CLASS_FIXED_DATA_PROCESSOR, "PRIMARY_ITEM_SHIFT", "I");
		assertFieldResolution(resolutionAtMiddle(resolver, sourceCode, "<< PRIMARY_ITEM_SHIFT;"),
				CLASS_FIXED_DATA_PROCESSOR, "PRIMARY_ITEM_SHIFT", "I");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "void main("),
				CLASS_FIXED_DATA_PROCESSOR, "main", "([Ljava/lang/String;)V");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "String[] args", 0),
				CLASS_STRING); // On type name yields type
		assertArrayResolution(resolutionAtOffset(resolver, sourceCode, "String[] args", "String[".length()),
				1, CLASS_STRING); // On brackets yields array
		assertVariableResolution(resolutionAtOffset(resolver, sourceCode, "String[] args", "String[] ar".length()),
				"args", "[Ljava/lang/String;"); // On use of the variable yield its variable resolution
		assertVariableResolution(resolutionAtMiddle(resolver, sourceCode, "args[0]"),
				"args", "[Ljava/lang/String;");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "args.length"),
				"I"); // Array length is a 'fake' field, so we yield the return type instead
		assertVariableResolution(resolutionAtMiddle(resolver, sourceCode, ", mappedOutput)"),
				"mappedOutput", CLASS_FIXED_DATA_LIST);
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "new ExampleFixedList<>"),
				CLASS_FIXED_DATA_LIST);
		assertClassResolution(resolutionAtStart(resolver, sourceCode, "new ExampleFixedList<>"),
				CLASS_FIXED_DATA_LIST);
		assertVariableResolution(resolutionAtOffset(resolver, sourceCode, ", ex)", 3),
				"ex",
				"java/io/IOException");
		assertVariableResolution(resolutionAtOffset(resolver, sourceCode, "String line = lines.get(i);", "String l".length()),
				"line", CLASS_STRING);
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "ng.join("), // a varargs method
				CLASS_STRING, "join", "(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "Files.writeString(path"), // another varargs method
				"java/nio/file/Files", "writeString", "(Ljava/nio/file/Path;Ljava/lang/CharSequence;[Ljava/nio/file/OpenOption;)Ljava/nio/file/Path;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "mapInput(lines)"),
				CLASS_FIXED_DATA_PROCESSOR, "mapInput", "(Ljava/util/List;)Lsample/ExampleFixedList;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "line.split(DATA"),
				CLASS_STRING, "split", "(Ljava/lang/String;)[Ljava/lang/String;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "lines.size();"),
				"java/util/List", "size", "()I");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "lines.get(i);"),
				"java/util/List", "get", "(I)Ljava/lang/Object;");
		assertResolvedMethodReturnType(resolutionAtMiddle(resolver, sourceCode, "lines.get(i);"),
				CLASS_STRING);
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "Missing argument, path to file"),
				CLASS_STRING);
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "primary:"),
				CLASS_STRING);
	}

	@Test
	void testOptionPanes() {
		String sourceCode = readSrc(CLASS_OPTION_PANES);
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertMultiMemberResolution(resolutionAtMiddle(resolver, sourceCode, "import static java.lang.Math.*;"),
				member -> assertEquals(CLASS_MATH, member.ownerEntry().getName()));
		assertMultiMemberResolution(resolutionAtMiddle(resolver, sourceCode, "import static javax.swing.JOptionPane.ERROR_MESSAGE;"),
				member -> {
					assertEquals("javax/swing/JOptionPane", member.ownerEntry().getName());
					assertEquals("ERROR_MESSAGE", member.memberEntry().getName());
					assertEquals("I", member.memberEntry().getDescriptor());
				});
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "JOptionPane.showMessageDialog(null, message, title, ERROR_MESSAGE);", 15),
				"javax/swing/JOptionPane", "showMessageDialog", "(Ljava/awt/Component;Ljava/lang/Object;Ljava/lang/String;I)V");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "parseInt(left)"),
				"java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, ".valueOf("),
				CLASS_STRING, "valueOf", "(I)Ljava/lang/String;"); // IntelliJ says it should be the "Object" receiver form, but ehh whatever close enough
	}

	@Test
	void testCharSeqMapper() {
		String sourceCode = readSrc(CLASS_CHAR_SEQ_MAPPER);
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtStart(resolver, sourceCode, "R map"),
				"java/lang/Object");
		assertClassResolution(resolutionAtStart(resolver, sourceCode, "CS c"),
				"java/lang/CharSequence");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "map(CS c);"),
				CLASS_CHAR_SEQ_MAPPER, "map", "(Ljava/lang/CharSequence;)Ljava/lang/Object;");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "map(CS c1, CS c2);"),
				CLASS_CHAR_SEQ_MAPPER, "map", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/Object;");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "map(CS... cs);"),
				CLASS_CHAR_SEQ_MAPPER, "map", "([Ljava/lang/CharSequence;)Ljava/lang/Object;");
	}

	@Test
	void testUnionThrowing() {
		String sourceCode = readSrc("sample/UnionThrowing");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "ClassNotFoundException"),
				"java/lang/ClassNotFoundException");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "NoSuchFieldException"),
				"java/lang/NoSuchFieldException");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "IllegalAccessException"),
				"java/lang/IllegalAccessException");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "printStackTrace"),
				"java/lang/Throwable", "printStackTrace", "()V");
	}

	@Test
	void testComputers() {
		String sourceCode = readSrc("sample/Computers");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "throws NoSuchMethodException"),
				"java/lang/NoSuchMethodException");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "@AnnoComputer"),
				"sample/AnnoComputer");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "@AnnoDisplay"),
				"sample/AnnoDisplay");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "@AnnoMotherboard"),
				"sample/AnnoMotherboard");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "(AnnoComputer.class)"),
				"sample/AnnoComputer");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "display = "),
				"sample/AnnoComputer", "display", "()Lsample/AnnoDisplay;");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "width = "),
				"sample/AnnoDisplay", "width", "()I");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "getDeclaredMethod"),
				"java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "println"),
				"java/io/PrintStream", "println", "(Ljava/lang/String;)V");
	}

	@Test
	void testNumbers() {
		String sourceCode = readSrc("sample/Numbers");
		CompilationUnitModel model = parser.parse(sourceCode);

		Resolver resolver = new BasicResolver(model, pool);

		assertFieldResolution(resolutionAtMiddle(resolver, sourceCode, "int[][] array2D = { "),
				"sample/Numbers$IntArrays", "array2D", "[[I");
	}

	@Test
	void testAnnoComputer() {
		String sourceCode = readSrc("sample/AnnoComputer");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "AnnoMotherboard m"),
				"sample/AnnoMotherboard");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "AnnoDisplay d"),
				"sample/AnnoDisplay");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "motherboard("),
				"sample/AnnoComputer", "motherboard", "()Lsample/AnnoMotherboard;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "display("),
				"sample/AnnoComputer", "display", "()Lsample/AnnoDisplay;");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "ElementType.T"),
				"java/lang/annotation/ElementType");
		assertFieldResolution(resolutionAtMiddle(resolver, sourceCode, ".TYPE,"),
				"java/lang/annotation/ElementType", "TYPE", "Ljava/lang/annotation/ElementType;");

		/*
		// TODO: Finicky case resolving the non-named, non-typed array use
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "({E"),
				"java/lang/annotation/Target", "value", "()[Ljava/lang/annotation/ElementType;");
		 */
	}

	@Test
	void testAnnoAnywhere() {
		String sourceCode = readSrc("sample/AnnotationsEverywhere");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "static @AnnoAnywhere String", 15),
				"sample/AnnoAnywhere");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "static @AnnoAnywhere String", 25),
				"java/lang/String");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "static String @AnnoAnywhere []", 10),
				"java/lang/String");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "static String @AnnoAnywhere []", 20),
				"sample/AnnoAnywhere");
	}

	@Test
	void testOverloadResolving() {
		String sourceCode = """
				package sample;

				import example.Arg;

				class OverloadsTwo {
					void foo(Arg arg) {}
					void foo(Arg arg, int i) {}

					void usage() {
						foo(new Arg());
						foo(new Arg(), 1);
					}
				}
				""";
		CompilationUnitModel model = parser.parse(sourceCode);

		// Create a pool for just this test with a new entry for the class we're modeling.
		// However, we won't register the 'Arg' type. This is to simulate the case where
		// the library is used in a context where we only have access to the 'OverloadsTwo' class.
		EntryPool pool2 = Utils.copy(pool);
		ClassEntry object = pool2.getClass("java/lang/Object"); // TODO: This is ugly, maybe make a util for this?
		ClassEntry arg = new BasicClassEntry("example/Arg", PUBLIC, object, List.of(), List.of(), null,
				List.of(), GenericTypes.ofClass(object), List.of(), List.of(), List.of());
		ClassEntry overloads = new BasicClassEntry("sample/OverloadsTwo", PUBLIC, object, List.of(), List.of(), null,
				List.of(), GenericTypes.ofClass(object), List.of(), List.of(), List.of(
						new BasicMethodEntry("foo", "(Lexample/Arg;)V", 0, GenericTypes.ofPrimitive(PrimitiveEntry.VOID), List.of(GenericTypes.ofClass(arg))),
						new BasicMethodEntry("foo", "(Lexample/Arg;I)V", 0, GenericTypes.ofPrimitive(PrimitiveEntry.VOID), List.of(GenericTypes.ofClass(arg), GenericTypes.ofPrimitive(PrimitiveEntry.INT)))));
		pool2.register(overloads);
		Resolver resolver = new BasicResolver(model, pool2);

		// Even without pool entries for the argument types, we should be able to differentiate
		// between the two 'foo' methods by the number of arguments.
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "foo(Arg arg) {}"),
				"sample/OverloadsTwo", "foo", "(Lexample/Arg;)V");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "foo(Arg arg, int i) {}"),
				"sample/OverloadsTwo", "foo", "(Lexample/Arg;I)V");

		// Same for the references.
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "foo(new Arg());"),
				"sample/OverloadsTwo", "foo", "(Lexample/Arg;)V");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "foo(new Arg(), 1);"),
				"sample/OverloadsTwo", "foo", "(Lexample/Arg;I)V");
	}

	@Test
	void testOuterClass() {
		String sourceCode = readSrc("sample/OuterClass");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "class InnerClass {"),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "new InnerClass();"),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "final InnerClass", 10),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "new OuterClass.InnerClass();", 10),
				"sample/OuterClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "new OuterClass.InnerClass();", 20),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "final OuterClass.InnerClass", 10),
				"sample/OuterClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "final OuterClass.InnerClass", 20),
				"sample/OuterClass$InnerClass");
		assertFieldResolution(resolutionAtMiddle(resolver, sourceCode, ".example);"),
				"sample/OuterClass$InnerClass", "example", "Ljava/lang/String;");
		assertFieldResolution(resolutionAtOffset(resolver, sourceCode, "return box.value;", 14),
				"sample/Box", "value", "Ljava/lang/Object;");
	}

	@Test
	void testQualifiedInnerClassGenericVariable() {
		String sourceCode = readSrc("sample/Extractor");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// The variable resolution should work regardless of whether we use qualified or unqualified inner class names.
		assertVariableResolution(resolutionAtOffset(resolver, sourceCode, "List<Item> list", "List<Item> l".length()),
				"list", "java/util/List");
		assertVariableResolution(resolutionAtOffset(resolver, sourceCode, "List<Extractor.Item> list", "List<Extractor.Item> l".length()),
				"list", "java/util/List");
	}

	@Test
	void testInnerClassInIsolation_CFR() {
		// Simulate scenario where the inner class is decompiled by CFR in isolation
		String sourceCode = """
				package sample;
				
				// Name of class is 'Outer.Inner' form, which javac does not like
				public class OuterClass.InnerClass {
					public String example = "Hello";
				
					private OuterClass.InnerClass() {
					    this("hello");
					}
				
					private OuterClass.InnerClass(String message) {
					    this.example = message;
					}
				
					String getExample() {
						return example;
					}
				
					@Override
					public String toString() {
						return example;
					}
				}
				""";
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// Inform the resolver that this is the declared class.
		ClassEntry inner = pool.getClass("sample/OuterClass$InnerClass");
		resolver.setDeclaredClass(model.getDeclaredClasses().getFirst(), inner);

		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "class OuterClass.InnerClass", 8),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "class OuterClass.InnerClass", 28),
				"sample/OuterClass$InnerClass");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "private OuterClass.InnerClass()"),
				"sample/OuterClass$InnerClass", "<init>", "(Lsample/OuterClass;)V");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "private OuterClass.InnerClass(String"),
				"sample/OuterClass$InnerClass", "<init>", "(Lsample/OuterClass;Ljava/lang/String;)V");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "getExample()"),
				"sample/OuterClass$InnerClass", "getExample", "()Ljava/lang/String;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "toString()"),
				"sample/OuterClass$InnerClass", "toString", "()Ljava/lang/String;");
	}

	@Test
	void testResolveMethodIfUnknownArgumentTypeCanBeInferred() {
		// Simulate scenario where we call some method we know of, but the argument type is unknown.
		// If we can infer the type we should be able to resolve the reference.
		String sourceCode = """
				package sample;
				
				public class OuterClass {
					@Override
					public String toString() {
						return String.copyValueOf(Unknown.foo, 0, 0)";
					}
				}
				""";
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "copyValueOf"),
				"java/lang/String", "copyValueOf", "([CII)Ljava/lang/String;");
	}

	@Test
	void testResolveFieldInArgumentContextWithoutRecursiveInference() {
		// Original failure path:
		// 1. Resolving 'TimeUnit.SECONDS' calls resolveFieldInContext(..., origin, "SECONDS").
		// 2. That asks inferFromUsage(...) for the argument's expected type from EnumSet.of(...).
		// 3. inferExpectedTypeForArgument(...) then inspects sibling arguments via collectKnownGenericArgumentHints(...).
		// 4. Resolving the sibling 'TimeUnit.MINUTES' repeats the same path and re-enters the original field inference.
		// 5. Without a re-entrancy guard this loops until StackOverflowError.
		//
		// We not have a guard so this should resolve just fine without blowing the stack.
		String sourceCode = """
				package sample;
				
				import java.util.EnumSet;
				import java.util.concurrent.TimeUnit;
				
				public class OuterClass {
					void test() {
						EnumSet.of(TimeUnit.SECONDS, TimeUnit.MINUTES);
					}
				}
				""";
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);
		MemberSelectExpressionModel fieldAccess = model.getRecursiveChildrenOfType(MemberSelectExpressionModel.class).stream()
				.filter(memberSelect -> memberSelect.getSource(model).trim().equals("TimeUnit.SECONDS"))
				.findFirst()
				.orElseThrow();
		assertFieldResolution(fieldAccess.resolve(resolver),
				"java/util/concurrent/TimeUnit", "SECONDS", "Ljava/util/concurrent/TimeUnit;");
	}

	@Test
	void testInnerClassInIsolation_Procyon() {
		// Simulate scenario where the inner class is decompiled by Procyon in isolation
		String sourceCode = """
				package sample;
				
				// Procyon doesn't include any hint that we're an inner class
				public class InnerClass {
					public String example = "Hello";
				
					// Isolated procyon decomp doesn't cleanup synthetic parameter
					InnerClass(final OuterClass this$0) {
						this.this$0 = this$0;
						final InnerClass = this;
					}
				
					String getExample() {
						return example;
					}
				
					@Override
					public String toString() {
						return new InnerClass().example;
					}
				}
				""";
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// Inform the resolver that this is the declared class.
		// The decompiled source implies this is a top-level class, but we know this to not be the case.
		ClassEntry inner = pool.getClass("sample/OuterClass$InnerClass");
		resolver.setDeclaredClass(model.getDeclaredClasses().getFirst(), inner);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "class InnerClass {"),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "new InnerClass()"),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "final InnerClass", 10),
				"sample/OuterClass$InnerClass");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "new InnerClass()", 10),
				"sample/OuterClass$InnerClass");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "InnerClass("),
				"sample/OuterClass$InnerClass", "<init>", "(Lsample/OuterClass;)V");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "final OuterClass this"),
				"sample/OuterClass");
	}

	@Test
	void testMultiCtor() {
		String sourceCode = readSrc("sample/MultiCtor");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "MultiCtor()"),
				"sample/MultiCtor", "<init>", "()V");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "MultiCtor(int i)"),
				"sample/MultiCtor", "<init>", "(I)V");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "MultiCtor(long j)"),
				"sample/MultiCtor", "<init>", "(J)V");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "MultiCtor(int i, long j)"),
				"sample/MultiCtor", "<init>", "(IJ)V");
	}

	@Test
	void testShape() {
		String sourceCode = readSrc("sample/Shape");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "Square"),
				"sample/Square");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "Circle"),
				"sample/Circle");
	}

	@Test
	void testSuperFoo() {
		String sourceCode = readSrc("sample/AFooServiceImplementation");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "extends AbstractFooService {"),
				"sample/AbstractFooService");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "super.foo()", 1),
				"sample/AbstractFooService");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "super.foo()", 8),
				"sample/AbstractFooService", "foo", "()V");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "super.toString()", 8),
				"java/lang/Object", "toString", "()Ljava/lang/String;");

		// Trickier case with the "super" being of the outer class.
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "AFooServiceImplementation.super.finalFoo()", 1),
				"sample/AFooServiceImplementation");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "AFooServiceImplementation.super.finalFoo()", 28),
				"sample/AbstractFooService");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "AFooServiceImplementation.super.finalFoo()", 38),
				"sample/AbstractFooService", "finalFoo", "()V");
	}

	@Test
	void testDefaultPackageResolve() {
		String sourceCode = readSrc("DefaultPackageClassTwo");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "DefaultPackageClassOne one = new DefaultPackageClassOne()", 1),
				"DefaultPackageClassOne");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "DefaultPackageClassOne one = new DefaultPackageClassOne()", 35),
				"DefaultPackageClassOne");
		assertFieldResolution(resolutionAtOffset(resolver, sourceCode, "DefaultPackageClassOne one = new DefaultPackageClassOne()", 24),
				"DefaultPackageClassTwo", "one", "LDefaultPackageClassOne;");

		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "DefaultPackageClassTwo TWO = new DefaultPackageClassTwo()", 1),
				"DefaultPackageClassTwo");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "DefaultPackageClassTwo TWO = new DefaultPackageClassTwo()", 35),
				"DefaultPackageClassTwo");
		assertFieldResolution(resolutionAtOffset(resolver, sourceCode, "DefaultPackageClassTwo TWO = new DefaultPackageClassTwo()", 24),
				"DefaultPackageClassTwo", "TWO", "LDefaultPackageClassTwo;");

		assertFieldResolution(resolutionAtOffset(resolver, sourceCode, "TWO.stuff();", 1),
				"DefaultPackageClassTwo", "TWO", "LDefaultPackageClassTwo;");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "TWO.stuff();", 7),
				"DefaultPackageClassTwo", "stuff", "()V");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "stuff(one);", 1),
				"DefaultPackageClassTwo", "stuff", "(LDefaultPackageClassOne;)V");
		assertFieldResolution(resolutionAtOffset(resolver, sourceCode, "stuff(one);", 7),
				"DefaultPackageClassTwo", "one", "LDefaultPackageClassOne;");
	}

	@Test
	void testMethodRefs() {
		String sourceCode = readSrc("sample/MethodRefs");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "::staticConsume"),
				"sample/BoxUseCases", "staticConsume", "(Lsample/Box;)V");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "::virtualConsume"),
				"sample/BoxUseCases", "virtualConsume", "(Lsample/Box;)V");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "newBox = Box::new", 15),
				"sample/Box", "<init>", "()V");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "newBoxWithArg = Box::new", 22),
				"sample/Box", "<init>", "(Ljava/lang/Object;)V");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "::length"),
				"java/lang/String", "length", "()I");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "::hashCode"),
				"java/lang/String", "hashCode", "()I");
	}

	@Test
	void testBoolCallable() {
		// The actual method code is in call()Boolean, but the class contract demands a call()Object
		//  - The Object returning method just delegates to the Boolean one
		//  - If we do a by-name lookup, we will get the call() from the Callable class and not the defining class BoolCallable
		//    because there are two methods of the name "call", so a by-name lookup is not good enough.
		//  - If we find that a descriptor backed lookup yields a "call()" in the defining class BoolCallable
		//    then that is a better match, and we will want to return that resolution instead.
		String sourceCode = readSrc("sample/BoolCallable");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "call("),
				"sample/BoolCallable", "call", "()Ljava/lang/Boolean;");
	}

	@Test
	void testJdk26EnumRegression() {
		// For some stupid reason JDK 26's javac range for enum constant's type model is populated to overlap
		// with the name of the enum constant's range. Brilliant. Anyways we have some stupid hacks in place to work around this.
		String sourceCode = readSrc("sample/SomeEnumeration");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertFieldResolution(resolutionAtStart(resolver, sourceCode, "ONE,"),
				"sample/SomeEnumeration", "ONE", "Lsample/SomeEnumeration;");

		assertFieldResolution(resolutionAtStart(resolver, sourceCode, "TWO,"),
				"sample/SomeEnumeration", "TWO", "Lsample/SomeEnumeration;");

		assertFieldResolution(resolutionAtStart(resolver, sourceCode, "THREE;"),
				"sample/SomeEnumeration", "THREE", "Lsample/SomeEnumeration;");
	}

	@Test
	void testBoxUseCases() {
		String sourceCode = readSrc("sample/BoxUseCases");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "Box<String> s"),
				"java/lang/String");
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "Box<String>("),
				"java/lang/String");
	}

	@Test
	void testVarLocals() {
		String sourceCode = readSrc("sample/VarLocals");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "getProperty("),
				"java/util/Properties", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "foo.split("),
				"java/lang/String", "split", "(Ljava/lang/String;)[Ljava/lang/String;");
		assertVariableResolution(resolutionAtMiddle(resolver, sourceCode, "fooList.length"),
				"fooList", "[Ljava/lang/String;");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "fooList.length", "fooList.l".length()),
				"I");
		assertVariableResolution(resolutionAtOffset(resolver, sourceCode, "var foo =", "var f".length()),
				"foo", CLASS_STRING);

	}

	@Test
	void testBoxUseCasesGenerics() {
		String sourceCode = readSrc("sample/BoxUseCases");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertFieldResolution(resolutionAtOffset(resolver, sourceCode, "stringBox.value.toUpperCase", "stringBox.v".length()),
				"sample/Box", "value", "Ljava/lang/Object;");
		assertResolvedFieldType(resolutionAtOffset(resolver, sourceCode, "stringBox.value.toUpperCase", "stringBox.v".length()),
				"java/lang/String");
		assertFieldResolution(resolutionAtOffset(resolver, sourceCode, "intBox.value.intValue", "intBox.v".length()),
				"sample/Box", "value", "Ljava/lang/Object;");
		assertResolvedFieldType(resolutionAtOffset(resolver, sourceCode, "intBox.value.intValue", "intBox.v".length()),
				"java/lang/Integer");
		assertResolvedFieldType(resolutionAtOffset(resolver, sourceCode, "wildcardBox.value.hashCode", "wildcardBox.v".length()),
				"java/lang/Object");
		assertResolvedFieldType(resolutionAtOffset(resolver, sourceCode, "numberBox.value.intValue", "numberBox.v".length()),
				"java/lang/Number");

		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "stringList.get(0).toUpperCase", "stringList.g".length()),
				"java/util/List", "get", "(I)Ljava/lang/Object;");
		assertResolvedMethodReturnType(resolutionAtOffset(resolver, sourceCode, "stringList.get(0).toUpperCase", "stringList.g".length()),
				"java/lang/String");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "fixedList.get(0).toUpperCase", "fixedList.g".length()),
				"sample/ExampleFixedList", "get", "(I)Ljava/lang/Object;");
		assertResolvedMethodReturnType(resolutionAtOffset(resolver, sourceCode, "fixedList.get(0).toUpperCase", "fixedList.g".length()),
				"java/lang/String");

		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "stringBox.value.toUpperCase", "stringBox.value.toUpper".length()),
				"java/lang/String", "toUpperCase", "()Ljava/lang/String;");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "stringList.get(0).toUpperCase()", "stringList.get(0).toUpper".length()),
				"java/lang/String", "toUpperCase", "()Ljava/lang/String;");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode,
						"listOListsOStrings.getFirst().getLast().toUpperCase()",
						"listOListsOStrings.getFirst().getLast().toUpper".length()),
				"java/lang/String", "toUpperCase", "()Ljava/lang/String;");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode,
						"get(\"a\").get(\"b\").get(\"c\").get(\"d\").toUpperCase()",
						"get(\"a\").get(\"b\").get(\"c\").get(\"d\").toUpper".length()),
				"java/lang/String", "toUpperCase", "()Ljava/lang/String;");

		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "get(0).get(0).toLowerCase()", "get(0).get(0).toLower".length()),
				"java/lang/String", "toLowerCase", "()Ljava/lang/String;");
	}

	@Test
	void testInstanceOf() {
		String sourceCode = readSrc("sample/InstanceOf");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "point instanceof Point2(int x, int y)", 20),
				"sample/InstanceOf$Point2");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "point instanceof Point3(int x, int y, int z)", 20),
				"sample/InstanceOf$Point3");
	}

	@Test
	void testLambdas() {
		String sourceCode = readSrc("sample/Lambdas");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// Support raw type inference
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "delegateRaw(Object::notify, new Object())", 23),
				"java/lang/Object", "notify", "()V");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "delegateRaw(raw -> raw.notify(), new Object())", 26),
				"java/lang/Object", "notify", "()V");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "Consumer rawConsumer = raw -> raw.notify()", 36),
				"java/lang/Object", "notify", "()V");

		// Our own interface with no generics
		String s1 = "return /* lambda */ root.length() + file.length() + flags";
		String s2 = "return /* inner-class */ root.length() + fileInner.length() + flagsInner";
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, s1, 30),
				"java/io/File", "length", "()J");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, s1, 45),
				"java/io/File", "length", "()J");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, s2, 35),
				"java/io/File", "length", "()J");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, s2, 55),
				"java/io/File", "length", "()J");

		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "delegateTyped(String::toLowerCase, \"string\")", 25),
				"java/lang/String", "toLowerCase", "()Ljava/lang/String;");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "delegateTyped(str -> str.toLowerCase(), \"string\")", 30),
				"java/lang/String", "toLowerCase", "()Ljava/lang/String;");
	}

	@Test
	void testScopeLookupAndDeepModelLookup() {
		String sourceCode = """
				class Example {
				    void test(String param) {
				        int outer = 1;
				        {
				            String inner = param;
				            inner.toUpperCase();
				        }
				    }
				}
				""";
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);
		int position = sourceCode.indexOf("inner.toUpperCase");

		Model leaf = model.getDeepestChildAtPosition(position);
		assertEquals("inner", leaf.getSource(model).trim());

		VariableModel inner = ScopeLookup.findVisibleVariable(model, position, "inner");
		VariableModel outer = ScopeLookup.findVisibleVariable(model, position, "outer");
		VariableModel param = ScopeLookup.findVisibleVariable(model, position, "param");
		assertNotNull(inner);
		assertNotNull(outer);
		assertNotNull(param);

		List<VariableModel> visible = ScopeLookup.collectVisibleVariables(model, position);
		assertTrue(visible.stream().anyMatch(variable -> variable.getName().equals("inner")));
		assertTrue(visible.stream().anyMatch(variable -> variable.getName().equals("outer")));
		assertTrue(visible.stream().anyMatch(variable -> variable.getName().equals("param")));

		assertVariableResolution(resolver.resolveReferenceAt("param", position), "param", CLASS_STRING);
		assertVariableResolution(resolver.resolveReferenceAt("outer", position), "outer", "I");
		assertVariableResolution(resolver.resolveReferenceAt("inner", position), "inner", CLASS_STRING);
	}

	@Test
	void testContextLookupHelpers() {
		String sourceCode = readSrc("sample/BoxUseCases");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// When resolving the 'Box<String>' type we have to resolve the 'Box' part first, which is a raw type.
		// Then we can resolve the type argument 'String' and apply that information back to the 'Box' resolution
		// to get a parameterized type resolution.
		Resolution boxResolution = resolutionAtOffset(resolver, sourceCode, "stringBox.value.toUpperCase();", 1);
		Resolution fieldResolution = resolver.resolveFieldInContext(boxResolution, "value");
		assertFieldResolution(fieldResolution, "sample/Box", "value", "Ljava/lang/Object;");
		assertResolvedFieldType(fieldResolution, CLASS_STRING);
		assertClassResolution(Resolutions.toValueTypeResolution(fieldResolution), CLASS_STRING);

		// The 'value' of the box is 'Object' as a field, but with generics we infer
		// it is a 'String' so we should be able to resolve 'toUpperCase' on it.
		Resolution methodResolution = resolver.resolveMethodInContext(fieldResolution, "toUpperCase", null, List.of());
		assertMethodResolution(methodResolution, "java/lang/String", "toUpperCase", "()Ljava/lang/String;");
		assertClassResolution(Resolutions.toValueTypeResolution(methodResolution), CLASS_STRING);

		String arraySourceCode = readSrc(CLASS_FIXED_DATA_PROCESSOR);
		CompilationUnitModel arrayModel = parser.parse(arraySourceCode);
		Resolver arrayResolver = new BasicResolver(arrayModel, pool);
		Resolution argsResolution = resolutionAtOffset(arrayResolver, arraySourceCode, "args[0]", 1);
		assertMethodResolution(arrayResolver.resolveMethodInContext(argsResolution, "clone", null, List.of()),
				"java/lang/Object", "clone", "()Ljava/lang/Object;");
	}

	@Test
	void testReferenceAndFragmentHelpers() {
		// Reuse the existing fixture so all referenced types are available in the shared test pool,
		// then rewrite one call site to include extra whitespace. That lets us prove the fragment
		// helpers are not coupled to exact formatting in the original fixture source.
		String sourceCode = readSrc("sample/BoxUseCases")
				.replace("stringBox.value.toUpperCase();", "stringBox . value . toUpperCase ( );");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);
		int position = sourceCode.indexOf("toUpperCase");

		// Reference resolution should preserve the kind of thing being referenced:
		// fields stay field resolutions, while visible types and keywords like 'this'
		// resolve to the containing class/type context.
		Resolution stringBoxResolution = resolver.resolveReferenceAt("stringBox", position);
		assertFieldResolution(stringBoxResolution, "sample/BoxUseCases", "stringBox", "Lsample/Box;");
		assertEquals("Lsample/Box;", Resolutions.getResolvedType(stringBoxResolution).getDescriptor());
		assertClassResolution(resolver.resolveReferenceAt("Box", position), "sample/Box");
		assertClassResolution(resolver.resolveReferenceAt("this", position), "sample/BoxUseCases");

		// Fragment resolution should keep member access as a field resolution so callers can still
		// inspect the owner/member identity, while the normalized resolved type reflects generic substitution.
		Resolution fieldResolution = resolver.resolveFragmentAt("stringBox.value", position);
		assertFieldResolution(fieldResolution, "sample/Box", "value", "Ljava/lang/Object;");
		assertResolvedFieldType(fieldResolution, CLASS_STRING);
		assertEquals("Ljava/lang/String;", Resolutions.getResolvedType(fieldResolution).getDescriptor());

		// Zero-arg invocation fragments should resolve all the way to the produced value type, which is
		// the main simplification Recaf can use instead of manually walking owners and generic return types.
		Resolution invocationResolution = resolver.resolveFragmentAt("stringBox.value.toUpperCase()", position);
		assertClassResolution(invocationResolution, CLASS_STRING);
		assertEquals("Ljava/lang/String;", Resolutions.getResolvedType(invocationResolution).getDescriptor());
	}

	@Test
	void testReferenceResolverHandlesDetachedQualifiedTypeName() {
		String sourceCode = readSrc("sample/MapLookup");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// The source uses wildcard-imported Map, while library users may ask for a detached fully-qualified name.
		assertClassResolution(resolver.resolveReferenceAt("java.util.Map", sourceCode.indexOf("headers.size")),
				"java/util/Map");
	}

	@Test
	void testReferenceResolverHandlesTypeParameterByBound() {
		String sourceCode = readSrc("sample/BoxAccess");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// We should be able to resolve a type parameter even if its:
		//  - Sourced by a method parameter like 'T value'
		//  - Which is backed by the class's type parameter 'T extends Number'
		assertClassResolution(resolver.resolveReferenceAt("T", sourceCode.indexOf("value.intValue")),
				"java/lang/Number");
	}

	@Test
	void testReferenceAndFragmentResolversIgnoreBlankInput() {
		String sourceCode = readSrc("sample/BoxAccess");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);
		int position = sourceCode.indexOf("value.intValue");

		// Blank input should not resolve to anything, even if the position is valid.
		assertUnknown(resolver.resolveReferenceAt(" ", position));
		assertUnknown(resolver.resolveFragmentAt(" ", position));
	}

	@Test
	void testWildcardLowerBoundBoxAccessResolvesThroughObject() {
		String sourceCode = readSrc("sample/BoxAccess");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// The 'lowerBox' is a Box<? super String>.
		// The real field type is 'Object' even though the generic type is a lower-bounded to '? super Integer'.
		// We don't want it to come back as some wildcard type, nor as 'Integer'.
		Resolution resolution = resolutionAtOffset(resolver, sourceCode, "lowerBox.value.toString()", "lowerBox.v".length());
		assertFieldResolution(resolution, "sample/Box", "value", "Ljava/lang/Object;");
		assertResolvedFieldType(resolution, "java/lang/Object");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "lowerBox.value.toString()", "lowerBox.value.toStr".length()),
				"java/lang/Object", "toString", "()Ljava/lang/String;");
	}

	@Test
	void testArrayMembersResolveFromVariablesAndArrayTypes() {
		String sourceCode = readSrc("sample/MessageTemplate");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// The 'recipients' variable is a String[] array.
		// We should be able to resolve the 'clone()' method and the 'length' field.
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "recipients.clone()", "recipients.cl".length()),
				"java/lang/Object", "clone", "()Ljava/lang/Object;");
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, "recipients.length", "recipients.l".length()),
				"I");

		// When we get the variable resolution for 'recipients' we expect it to be String[].
		Resolution recipientsVariable = resolutionAtStart(resolver, sourceCode, "recipients.length");
		assertVariableResolution(recipientsVariable, "recipients", "[Ljava/lang/String;");

		// In the context of the variable, the field 'length' should resolve to the primitive int type.
		assertClassResolution(resolver.resolveFieldInContext(recipientsVariable, "length"), "I");

		// Resolving on 'String' in the 'String[]' of the array variable should get us the array type.
		// We should be able to resolve 'clone()' and 'length' on the array type as well.
		Resolution recipientsArray = resolutionAtOffset(resolver, sourceCode, "String[] recipients", "String[".length());
		assertArrayResolution(recipientsArray, 1, CLASS_STRING);
		assertMethodResolution(resolver.resolveMethodInContext(recipientsArray, "clone", null, List.of()),
				"java/lang/Object", "clone", "()Ljava/lang/Object;");
		assertClassResolution(resolver.resolveFieldInContext(recipientsArray, "length"), "I");
		assertMethodResolution(resolver.resolveMethodInContext(recipientsArray, "clone", null, List.of()),
				"java/lang/Object", "clone", "()Ljava/lang/Object;");
	}

	@Test
	void testMethodReturnContextKeepsGenericFieldType() {
		String sourceCode = readSrc("sample/BoxAccess");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// The 'provideBox()' method returns a Box<String>.
		Resolution provideBox = resolutionAtOffset(resolver, sourceCode, "provideBox().value.toUpperCase()", "provide".length());
		assertMethodResolution(provideBox, "sample/BoxAccess", "provideBox", "()Lsample/Box;");

		// In the Box<String> context we should be able to resolve the 'value' field to a String type.
		Resolution providedValue = resolver.resolveFieldInContext(provideBox, "value");
		assertFieldResolution(providedValue, "sample/Box", "value", "Ljava/lang/Object;");
		assertResolvedFieldType(providedValue, CLASS_STRING);
	}

	@Test
	void testInheritedAndStaticMembersInTemplateHierarchy() {
		String sourceCode = readSrc("sample/MessageTemplate");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		// The 'status' field is declared in MessageTemplate, so as a baseline this should work fine.
		assertFieldResolution(resolutionAtStart(resolver, sourceCode, "status.length()"),
				"sample/MessageTemplate", "status", "Ljava/lang/String;");

		// MessageTemplate extends MessageTemplateBase, which declares 'fallbackTitle' and 'defaultPrefix'.
		// We should be able to resolve those fields and their accessors as well.
		assertFieldResolution(resolutionAtStart(resolver, sourceCode, "fallbackTitle.length()"),
				"sample/MessageTemplateBase", "fallbackTitle", "Ljava/lang/String;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "fallbackTitle()"),
				"sample/MessageTemplateBase", "fallbackTitle", "()Ljava/lang/String;");
		assertFieldResolution(resolutionAtStart(resolver, sourceCode, "defaultPrefix.length()"),
				"sample/MessageTemplateBase", "defaultPrefix", "Ljava/lang/String;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "defaultPrefix()"),
				"sample/MessageTemplateBase", "defaultPrefix", "()Ljava/lang/String;");
	}

	@Test
	void testReferenceHelpersUseEnclosingMetadataWhenSourceIsIncomplete() {
		// Make a unit with less information than the real MessageTemplate class.
		// It is still syntactically valid, but it doesn't declare the base class or any of the fields.
		String sourceCode = """
				package sample;

				public class MessageTemplate {
					void detached() {
						System.nanoTime();
					}
				}
				""";
		CompilationUnitModel model = parser.parse(sourceCode);

		// Our resolver specifies the real MessageTemplate class as the declared class, so it can still resolve inherited and static members.
		Resolver resolver = new BasicResolver(model, pool);
		resolver.setDeclaredClass(model.getDeclaredClasses().getFirst(), pool.getClass("sample/MessageTemplate"));

		// Bogus position, but we can still resolve the inherited and static members by name
		// because the resolver has the real class metadata.
		int position = sourceCode.indexOf("nanoTime");
		assertFieldResolution(resolver.resolveReferenceAt("fallbackTitle", position),
				"sample/MessageTemplateBase", "fallbackTitle", "Ljava/lang/String;");
		assertFieldResolution(resolver.resolveReferenceAt("status", position),
				"sample/MessageTemplate", "status", "Ljava/lang/String;");
		assertUnknown(resolver.resolveReferenceAt("notAField", position));
	}

	@Test
	void testStaticInitializerResolution() {
		// Even if we use the default reflective entry pool model, which has no way to know about the existence
		// of static initializer methods, we should resolve the <clinit> if we resolve on the static {} block.
		String sourceCode = readSrc("sample/StaticInitBacked");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "static {"),
				"sample/StaticInitBacked", "<clinit>", "()V");
	}

	private static void assertPackageResolution(Resolution resolution, String name) {
		if (resolution instanceof PackageResolution packageResolution) {
			if (name != null) assertEquals(name, packageResolution.getPackageName());
		} else {
			fail("Resolution was not of a package: " + resolution);
		}
	}

	private static void assertUnknown(Resolution resolution) {
		assertTrue(resolution.isUnknown(), "Resolution should be unknown: " + resolution);
	}

	private static void assertMultiMemberResolution(Resolution resolution, Consumer<ClassMemberPair> consumer) {
		if (resolution instanceof MultiMemberResolution multiMemberResolution) {
			multiMemberResolution.getMemberEntries().forEach(consumer);
		} else {
			fail("Resolution was not of a multi-member: " + resolution);
		}
	}

	private static void assertClassResolution(Resolution resolution, String name) {
		switch (resolution) {
			case ClassResolution classResolution -> {
				if (name != null) assertEquals(name, classResolution.getClassEntry().getName());
			}
			case PrimitiveResolution primitiveResolution -> {
				if (name != null) assertEquals(name, primitiveResolution.getPrimitiveEntry().getDescriptor());
			}
			case ArrayResolution arrayResolution -> {
				if (name != null) assertEquals(name, arrayResolution.getArrayEntry().getDescriptor());
			}
			case null, default -> fail("Resolution was not of a class: " + resolution);
		}
	}

	private static void assertMethodResolution(Resolution resolution, String owner, String name, String desc) {
		if (resolution instanceof MethodResolution methodResolution) {
			ClassEntry ownerEntry = methodResolution.getOwnerEntry();
			MethodEntry methodEntry = methodResolution.getMethodEntry();
			if (owner != null) assertEquals(owner, ownerEntry.getName());
			if (name != null) assertEquals(name, methodEntry.getName());
			if (desc != null) assertEquals(desc, methodEntry.getDescriptor());
		} else {
			fail("Resolution was not of a method: " + resolution);
		}
	}

	private static void assertFieldResolution(Resolution resolution, String owner, String name, String desc) {
		if (resolution instanceof FieldResolution methodResolution) {
			ClassEntry ownerEntry = methodResolution.getOwnerEntry();
			FieldEntry fieldEntry = methodResolution.getFieldEntry();
			if (owner != null) assertEquals(owner, ownerEntry.getName());
			if (name != null) assertEquals(name, fieldEntry.getName());
			if (desc != null) assertEquals(desc, fieldEntry.getDescriptor());
		} else {
			fail("Resolution was not of a field: " + resolution);
		}
	}

	private static void assertVariableResolution(Resolution resolution, String name, String descOrName) {
		if (resolution instanceof VariableResolution variableResolution) {
			if (name != null) assertEquals(name, variableResolution.getName());
			if (descOrName != null) {
				DescribableEntry resolvedType = variableResolution.getResolvedType();
				assertEquals(descOrName, resolvedType instanceof ClassEntry classEntry ? classEntry.getName() : resolvedType.getDescriptor());
			}
		} else {
			fail("Resolution was not of a variable: " + resolution);
		}
	}

	private static void assertResolvedMethodReturnType(Resolution resolution, String descOrName) {
		if (resolution instanceof MethodResolution methodResolution) {
			DescribableEntry resolvedReturnType = methodResolution.getResolvedReturnType();
			if (descOrName != null)
				assertEquals(descOrName, resolvedReturnType instanceof ClassEntry classEntry ? classEntry.getName() : resolvedReturnType.getDescriptor());
		} else {
			fail("Resolution was not of a method: " + resolution);
		}
	}

	private static void assertResolvedFieldType(Resolution resolution, String descOrName) {
		if (resolution instanceof FieldResolution fieldResolution) {
			DescribableEntry resolvedFieldType = fieldResolution.getResolvedFieldType();
			if (descOrName != null)
				assertEquals(descOrName, resolvedFieldType instanceof ClassEntry classEntry ? classEntry.getName() : resolvedFieldType.getDescriptor());
		} else {
			fail("Resolution was not of a field: " + resolution);
		}
	}

	private static void assertArrayResolution(Resolution resolution, int dimensions, String elementName) {
		if (resolution instanceof ArrayResolution arrayResolution) {
			assertEquals(dimensions, arrayResolution.getDescribableEntry().getDimensions());
			if (elementName != null) {
				DescribableResolution elementResolution = arrayResolution.getElementTypeResolution();
				if (elementResolution instanceof ClassResolution classResolution)
					assertEquals(elementName, classResolution.getClassEntry().getName());
				else
					assertEquals(elementName, elementResolution.getDescribableEntry().getDescriptor());
			}
		} else {
			fail("Resolution was not of an array: " + resolution);
		}
	}

	private static Resolution resolutionAtStart(Resolver resolver, String source, String toMatch) {
		return resolutionAtOffset(resolver, source, toMatch, 0);
	}

	private static Resolution resolutionAtMiddle(Resolver resolver, String source, String toMatch) {
		return resolutionAtOffset(resolver, source, toMatch, toMatch.length() / 2);
	}

	private static Resolution resolutionAtOffset(Resolver resolver, String source, String toMatch, int offset) {
		int index = source.indexOf(toMatch);
		if (index < 0)
			fail("Match does not exist in source: " + toMatch);
		index += offset;
		return resolver.resolveAt(index);
	}

	private static String readSrc(String name) {
		try {
			return Files.readString(Paths.get("src/testFixtures/java/" + name + ".java"));
		} catch (IOException ex) {
			fail("Failed to read input : " + name, ex);
			throw new IllegalStateException();
		}
	}
}

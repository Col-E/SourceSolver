package software.coley.sourcesolver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sample.OuterClass;
import software.coley.sourcesolver.model.CompilationUnitModel;
import software.coley.sourcesolver.resolve.BasicResolver;
import software.coley.sourcesolver.resolve.Resolver;
import software.coley.sourcesolver.resolve.entry.ClassEntry;
import software.coley.sourcesolver.resolve.entry.ClassMemberPair;
import software.coley.sourcesolver.resolve.entry.EntryPool;
import software.coley.sourcesolver.resolve.entry.FieldEntry;
import software.coley.sourcesolver.resolve.entry.MethodEntry;
import software.coley.sourcesolver.resolve.result.ArrayResolution;
import software.coley.sourcesolver.resolve.result.ClassResolution;
import software.coley.sourcesolver.resolve.result.DescribableResolution;
import software.coley.sourcesolver.resolve.result.FieldResolution;
import software.coley.sourcesolver.resolve.result.MethodResolution;
import software.coley.sourcesolver.resolve.result.MultiMemberResolution;
import software.coley.sourcesolver.resolve.result.PackageResolution;
import software.coley.sourcesolver.resolve.result.PrimitiveResolution;
import software.coley.sourcesolver.resolve.result.Resolution;
import software.coley.sourcesolver.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
		assertArrayResolution(resolutionAtOffset(resolver, sourceCode, "String[] args", "String[] ar".length()),
				1, CLASS_STRING); // On use of the variable yield its type (array)
		assertArrayResolution(resolutionAtMiddle(resolver, sourceCode, "args[0]"),
				1, CLASS_STRING);
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "args.length"),
				"I"); // Array length is a 'fake' field, so we yield the return type instead
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, ", mappedOutput)"),
				CLASS_FIXED_DATA_LIST); // Use of the variable will yield its type
		assertClassResolution(resolutionAtMiddle(resolver, sourceCode, "new ExampleFixedList<>"),
				CLASS_FIXED_DATA_LIST);
		assertClassResolution(resolutionAtStart(resolver, sourceCode, "new ExampleFixedList<>"),
				CLASS_FIXED_DATA_LIST);
		assertClassResolution(resolutionAtOffset(resolver, sourceCode, ", ex)", 3),
				"java/io/IOException");
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
	}

	@Test
	void testInnerClassInIsolation_CFR() {
		// Simulate scenario where the inner class is decompiled by CFR in isolation
		String sourceCode = """
				package sample;
								
				// Name of class is 'Outer.Inner' form, which javac does not like
				public class OuterClass.InnerClass {
					public String example = "Hello";
					
					public OuterClass.InnerClass() {
				        this.example = "Hello";
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
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "OuterClass.InnerClass("),
				"sample/OuterClass$InnerClass", "<init>", "(Lsample/OuterClass;)V");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "getExample()"),
				"sample/OuterClass$InnerClass", "getExample", "()Ljava/lang/String;");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "toString()"),
				"sample/OuterClass$InnerClass", "toString", "()Ljava/lang/String;");
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
	void testMethodRefs() {
		String sourceCode = readSrc("sample/MethodRefs");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "::staticConsume"),
				"sample/BoxUseCases", "staticConsume", "(Lsample/Box;)V");
		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "::virtualConsume"),
				"sample/BoxUseCases", "virtualConsume", "(Lsample/Box;)V");
		/* TODO: Need to pull surrounding context to differentiate ambiguous references
		    - This may also be blocked by the generics system needing to be implemented
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "newBox = Box::new", 15),
				"sample/Box", "<init>", "()V");
		assertMethodResolution(resolutionAtOffset(resolver, sourceCode, "newBoxWithArg = Box::new", 22),
				"sample/Box", "<init>", "(Ljava/lang/Object;)V");
		 */
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
	@Disabled("Generic resolution required")
	void testBoxUseCases() {
		// TODO: Need to create a system to resolve things with generics
		//  IE if a field is a "T" signature, then I should adapt the field-resolution to be of bound "T" instead of "Object"
		//  And if I have List<T> then "get(int)" which yields T should also be adapted to the bound.
		String sourceCode = readSrc("sample/BoxUseCases");
		CompilationUnitModel model = parser.parse(sourceCode);
		Resolver resolver = new BasicResolver(model, pool);

		assertMethodResolution(resolutionAtMiddle(resolver, sourceCode, "toUpperCase"),
				"java/lang/String", "toUpperCase", "()Ljava/lang/String;");
		assertMethodResolution(resolutionAtStart(resolver, sourceCode, "intValue"),
				"java/lang/Integer", "intValue", "()I");
	}

	private static void assertPackageResolution(Resolution resolution, String name) {
		if (resolution instanceof PackageResolution packageResolution) {
			if (name != null) assertEquals(name, packageResolution.getPackageName());
		} else {
			fail("Resolution was not of a package: " + resolution);
		}
	}

	private static void assertMultiMemberResolution(Resolution resolution, Consumer<ClassMemberPair> consumer) {
		if (resolution instanceof MultiMemberResolution multiMemberResolution) {
			multiMemberResolution.getMemberEntries().forEach(consumer);
		} else {
			fail("Resolution was not of a multi-member: " + resolution);
		}
	}

	private static void assertClassResolution(Resolution resolution, String name) {
		if (resolution instanceof ClassResolution classResolution) {
			if (name != null) assertEquals(name, classResolution.getClassEntry().getName());
		} else if (resolution instanceof PrimitiveResolution primitiveResolution) {
			if (name != null) assertEquals(name, primitiveResolution.getPrimitiveEntry().getDescriptor());
		} else {
			fail("Resolution was not of a class: " + resolution);
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
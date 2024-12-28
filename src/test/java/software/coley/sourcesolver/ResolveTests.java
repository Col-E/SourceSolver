package software.coley.sourcesolver;

import org.junit.jupiter.api.Test;
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
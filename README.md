# Source-Solver

A lightweight wrapper on the `javac` API to support resolving class and member references in a `String` of Java source code.

## Example use

```java
// Provide some Java source code
String sourceCode = Files.readString(Paths.get("Checkbox.java"));
int index = sourceCode.indexOf("void processItemEvent(") + 15;

// Create a parser and parse the source into the Source-Solver model
Parser parser = new Parser();
CompilationUnitModel model = parser.parse(sourceCode);

// Create an entry-pool which provides information about class structures to Source-Solver.
// You are expected to provide your own ClassEntry instances into the pool.
EntryPool pool = new BasicEntryPool();
loadClassesIntoPool(pool);

// Create a resolver targeting the given compilation unit and resolve at some index
// into the 'sourceCode' string.
Resolver resolver = new BasicResolver(model, pool);
Resolution resolution = resolver.resolveAt(index);

// Check for an instance of the expected resolution type to get more information about the resolved
// class or member reference at the given index.
if (resolution instanceof MethodResolution methodResolution) { ... }
```

## Requirements

You will need to provide access to the following modules when using this library:

```
jdk.compiler/com.sun.tools.javac.parser
jdk.compiler/com.sun.tools.javac.tree
jdk.compiler/com.sun.tools.javac.util
```
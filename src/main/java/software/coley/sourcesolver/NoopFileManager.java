package software.coley.sourcesolver;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Minimal file manager implementation for javac.
 *
 * @author Matt Coley
 */
public class NoopFileManager implements JavaFileManager {
	@Override
	public ClassLoader getClassLoader(Location location) {
		return ClassLoader.getSystemClassLoader();
	}

	@Override
	public boolean isSameFile(FileObject a, FileObject b) {
		return Objects.equals(a.toUri(), b.toUri());
	}

	@Override
	public Iterable<JavaFileObject> list(Location location,
	                                     String packageName,
	                                     Set<JavaFileObject.Kind> kinds,
	                                     boolean recurse) {
		// Yield no matches
		return Collections.emptyList();
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		// Yield null to indicate no match in the given location
		return null;
	}

	@Override
	public boolean handleOption(String current, Iterator<String> remaining) {
		// Nop options are handed by this no-op file manager
		return false;
	}

	@Override
	public boolean hasLocation(Location location) {
		// All locations are unknown to this no-op file-manager
		return false;
	}

	@Override
	public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) {
		// All locations/siblings are unknown to this no-op file-manager
		throw new IllegalArgumentException("getJavaFileForInput: location: " + location +
				", className: " + className +
				", kind: " + kind
		);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
		// All locations/siblings are unknown to this no-op file-manager
		throw new IllegalArgumentException("getJavaFileForOutput: location: " + location +
				", className: " + className +
				", kind: " + kind +
				", sibling: " + sibling
		);
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName, String relativeName) {
		// All locations are unknown to this no-op file-manager
		throw new IllegalArgumentException("getFileForInput: location: " + location +
				", packageName: " + packageName +
				", relativeName: " + relativeName
		);
	}

	@Override
	public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) {
		// All locations/siblings are unknown to this no-op file-manager
		throw new IllegalArgumentException("getFileForOutput: location: " + location +
				", packageName: " + packageName +
				", relativeName: " + relativeName +
				", sibling: " + sibling
		);
	}

	@Override
	public int isSupportedOption(String option) {
		// Default to supporting nothing
		return -1;
	}

	@Override
	public void flush() {}

	@Override
	public void close() {}
}

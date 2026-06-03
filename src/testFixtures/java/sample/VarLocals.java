package sample;

public class VarLocals {
	String[] getFoos() {
		var map = System.getProperties();
		var foo = map.getProperty("foo");
		var fooList = foo.split(",");
		System.out.println("There are " + fooList.length + " foos.");
		return fooList;
	}
}
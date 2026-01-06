package sample;

public class InstanceOf {
	IPoint point;

	InstanceOf(IPoint point) {
		this.point = point;
	}

	int sum() {
		if (point instanceof Point2(int x, int y)) {
			return x + y;
		} else if (point instanceof Point3(int x, int y, int z)) {
			return x + y + z;
		} else {
			return 0;
		}
	}

	sealed interface IPoint permits Point2, Point3 {}

	record Point2(int x, int y) implements IPoint {}

	record Point3(int x, int y, int z) implements IPoint {}
}

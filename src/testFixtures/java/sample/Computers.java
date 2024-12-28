package sample;

public class Computers {
	@AnnoComputer(
			display = @AnnoDisplay(width = 1920, height = 1080),
			motherboard = @AnnoMotherboard(ram = 100)
	)
	AnnoComputer basic() throws NoSuchMethodException {
		return Computers.class.getDeclaredMethod("basic")
				.getAnnotation(AnnoComputer.class);
	}

	void printBasicResolution() throws NoSuchMethodException {
		AnnoDisplay display = basic().display();
		System.out.println(display.width() + "x" + display.height());
	}
}

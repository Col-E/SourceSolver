package sample;

import javax.swing.JOptionPane;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.IntSupplier;

import static java.lang.Integer.parseInt;
import static java.lang.Math.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

public class OptionPanes {
	public static void showInfo(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, INFORMATION_MESSAGE);
	}

	public static void showError(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, ERROR_MESSAGE);
	}

	public static void showMath(String left, String right, String operation) {
		String expression = left + " " + operation + " " + right;
		int l = parseInt(left);
		int r = parseInt(right);
		IntSupplier intSupplier = OptionPanes::localFunc;
		String value = String.valueOf(switch (operation) {
			case "--" -> incrementExact(l);
			case "++" -> decrementExact(l);
			case "+" -> addExact(l, r);
			case "-" -> subtractExact(l, r);
			case "*" -> multiplyExact(l, r);
			case "/" -> (l / r);
			case "min" -> min(l, r);
			case "max" -> max(l, r);
			case "%" -> (l % r);
			case "m1" -> intSupplier.getAsInt();
			case "osHash" -> System.getProperty("os.name").hashCode();
			case "fixedRand1" -> Numbers.INSTANCE.integers.rand1;
			//case "fixedRand2" -> Numbers.INSTANCE.integerArrays.array.length;
			case "rand" -> {
				int op = (int) (l + (random() * r));
				yield abs(op);
			}
			default -> throw new IllegalStateException("Unexpected operation: " + operation);
		});
		showInfo("Computation: " + expression, value);
	}

	private static int localFunc() {
		return -1;
	}
}

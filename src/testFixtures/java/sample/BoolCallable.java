package sample;

import java.util.concurrent.Callable;

public class BoolCallable implements Callable<Boolean> {
	@Override
	public Boolean call() {
		return false;
	}
}

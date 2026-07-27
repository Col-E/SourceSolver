package sample;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

public class ThreadUtil {
	public static <T> CompletableFuture<T> blockUntilDone(Function<Executor, CompletableFuture<T>> task) {
		return ThreadUtil.blockUntilDone(task, CompletableFuture::isDone);
	}

	public static <T> T blockUntilDone(Function<Executor, T> task, Predicate<T> completionCheck) {
		return null;
	}
}

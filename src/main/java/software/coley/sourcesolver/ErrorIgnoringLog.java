package software.coley.sourcesolver;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.PrintWriter;
import java.util.function.Consumer;

/**
 * Logger implementation which ignores errors in the tree model.
 *
 * @author Matt Coley
 * @see #setErrorListener(Consumer) Errors can be observed via a listener.
 */
public class ErrorIgnoringLog extends Log {
	private Consumer<Throwable> errorListener;

	public ErrorIgnoringLog(@Nonnull Context context) {
		super(context);
	}

	public ErrorIgnoringLog(@Nonnull Context context, PrintWriter writer) {
		super(context, writer);
	}

	public ErrorIgnoringLog(@Nonnull Context context, PrintWriter out, PrintWriter err) {
		super(context, out, err);
	}

	/**
	 * @param errorListener
	 * 		Listener to observe errors.
	 */
	public void setErrorListener(@Nullable Consumer<Throwable> errorListener) {
		this.errorListener = errorListener;
	}

	@Override
	public void error(String key, Object... args) {
		try {
			super.error(key, args);
		} catch (Throwable t) {
			if (errorListener != null) errorListener.accept(t);
		}
	}

	@Override
	public void error(JCDiagnostic.Error errorKey) {
		try {
			super.error(errorKey);
		} catch (Throwable t) {
			if (errorListener != null) errorListener.accept(t);
		}
	}

	@Override
	public void error(JCDiagnostic.DiagnosticPosition pos, JCDiagnostic.Error errorKey) {
		try {
			super.error(pos, errorKey);
		} catch (Throwable t) {
			if (errorListener != null) errorListener.accept(t);
		}
	}

	@Override
	public void error(JCDiagnostic.DiagnosticFlag flag, JCDiagnostic.DiagnosticPosition pos, JCDiagnostic.Error errorKey) {
		try {
			super.error(flag, pos, errorKey);
		} catch (Throwable t) {
			if (errorListener != null) errorListener.accept(t);
		}
	}

	@Override
	public void error(int pos, String key, Object... args) {
		try {
			super.error(pos, key, args);
		} catch (Throwable t) {
			if (errorListener != null) errorListener.accept(t);
		}
	}

	@Override
	public void error(int pos, JCDiagnostic.Error errorKey) {
		try {
			super.error(pos, errorKey);
		} catch (Throwable t) {
			if (errorListener != null) errorListener.accept(t);
		}
	}

	@Override
	public void error(JCDiagnostic.DiagnosticFlag flag, int pos, JCDiagnostic.Error errorKey) {
		try {
			super.error(flag, pos, errorKey);
		} catch (Throwable t) {
			if (errorListener != null) errorListener.accept(t);
		}
	}
}

package sample;

public class MessageTemplateBase {
	protected String fallbackTitle = "Welcome";
	protected static String defaultPrefix = "message";

	protected String fallbackTitle() {
		return fallbackTitle;
	}

	protected static String defaultPrefix() {
		return defaultPrefix;
	}
}

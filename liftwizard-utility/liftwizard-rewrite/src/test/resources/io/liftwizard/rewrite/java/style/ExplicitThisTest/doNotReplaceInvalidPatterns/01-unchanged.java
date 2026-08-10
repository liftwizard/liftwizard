import java.util.function.Consumer;

class Test {
	private String field;
	private static String staticField;

	void instanceMethod(String parameter) {
		this.field = "already has this";
		String localVariable = parameter;
		String result = parameter + localVariable;
	}

	static void staticMethod() {
		staticField = "static context";
	}
}

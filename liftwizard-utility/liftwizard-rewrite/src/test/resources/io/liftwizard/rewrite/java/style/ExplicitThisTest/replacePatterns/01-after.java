import java.util.function.Consumer;

class Parent
{
	private String parentField;

	Parent(String value)
	{
		this.parentField = value;
	}
}

class Test
	extends Parent
{
	private String field;
	private String field1;
	private String field2;
	private static String staticField;
	private String alreadyPrefixed;

	private String fieldInit1 = "initial";
	private String fieldInit2 = this.field1;
	private String fieldInit3 = this.field1 + this.field2;

	private static String staticFieldInit = staticField;

	static
	{
		staticField = "static initializer";
		staticHelper();
	}

	{
		this.field = "instance initializer";
		this.field1 = this.field2;
	}

	Test(String value)
	{
		super(value);
		this.field = "constructor";
		this.field1 = this.field2;
	}

	Test()
	{
		this("default");
	}

	void instanceMethod(String parameter)
	{
		this.field = "value";
		this.field1 = this.field2;
		this.helper();
		staticField = "static context";
		staticMethod();

		String localVariable = parameter;
		String result = parameter + localVariable;

		this.alreadyPrefixed = "already has this";
		this.alreadyPrefixedMethod();
		super.toString();

		Consumer<String> lambda = (s) ->
		{
			this.field = s;
			this.field1 = this.field2;
		};

		Runnable runnable = () -> this.field = "lambda";
	}

	static void staticMethod()
	{
		staticField = "static context";
		staticHelper();
	}

	void helper()
	{
	}

	void alreadyPrefixedMethod()
	{
	}

	static void staticHelper()
	{
	}
}

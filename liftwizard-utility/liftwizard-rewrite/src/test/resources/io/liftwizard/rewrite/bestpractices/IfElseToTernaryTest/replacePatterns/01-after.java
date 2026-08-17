import java.util.List;
import java.util.Map;

class Test
{
	static class Builder
	{
		Builder method1(Object o)
		{
			return this;
		}

		Builder method2(Object o)
		{
			return this;
		}

		Builder method3(Object o)
		{
			return this;
		}

		void method4()
		{
		}
	}

	Builder field = new Builder();

	void differingArgument(List<String> list, boolean flag, String first, String second)
	{
		list.add(flag ? first : second);
	}

	void differingNonFirstArgument(Map<String, String> map, boolean flag, String key, String first, String second)
	{
		map.put(key, flag ? first : second);
	}

	void differingReceiver(List<String> left, List<String> right, boolean flag, String value)
	{
		(flag ? left : right).add(value);
	}

	String differingAssignment(boolean flag, String first, String second)
	{
		String result;
		result = flag ? first : second;
		return result;
	}

	void deeplyNestedDifferingArgument(boolean condition, Object local)
	{
		Object CONSTANT = null;
		Object VALUE_A = null;
		Object VALUE_B = null;
		this.field.method1(CONSTANT).method2(local).method3(condition ? VALUE_A : VALUE_B).method4();
	}

	void deeplyNestedDifferingReceiver(List<String> left, List<String> right, boolean flag, String value)
	{
		(flag ? left : right).subList(0, 1).add(value);
	}

	void elseIfBody(List<String> list, boolean first, boolean second, String a, String b)
	{
		if (first)
		{
			list.add("first");
		}
		else {
			list.add(second ? a : b);
		}
	}
}

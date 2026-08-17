import java.util.Map;

class Test
{
	void test(Map<String, Integer> map)
	{
		boolean simpleNullCheck = map == null;
		boolean simpleIsEmptyCheck = map.isEmpty();
		boolean wrongOperator = map != null || !map.isEmpty();
	}
}

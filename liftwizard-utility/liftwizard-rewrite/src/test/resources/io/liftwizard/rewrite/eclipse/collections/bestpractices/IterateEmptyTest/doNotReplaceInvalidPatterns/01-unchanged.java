import java.util.List;

class Test
{
	void test(List<String> list)
	{
		boolean simpleNullCheck = list == null;
		boolean simpleIsEmptyCheck = list.isEmpty();
		boolean wrongOperator = list != null || !list.isEmpty();
	}
}

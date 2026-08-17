import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean result1 = list.anySatisfy(String::isEmpty);
		boolean result2 = list.allSatisfy((each) -> each.length() > 1);
		boolean result3 = list.noneSatisfy(String::isBlank);
		boolean result4 = list
			.select((each) -> !each.isEmpty())
			.anySatisfy((each) -> each.length() > 3);
		list.forEach(System.out::println);
	}
}

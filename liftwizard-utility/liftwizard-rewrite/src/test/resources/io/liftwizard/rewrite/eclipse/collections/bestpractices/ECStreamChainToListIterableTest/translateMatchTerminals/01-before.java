import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean result1 = list.stream().anyMatch(String::isEmpty);
		boolean result2 = list.stream().allMatch((each) -> each.length() > 1);
		boolean result3 = list.stream().noneMatch(String::isBlank);
		boolean result4 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.anyMatch((each) -> each.length() > 3);
		list.stream().forEach(System.out::println);
	}
}

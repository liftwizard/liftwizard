import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		var result1 = list.stream().toList();
		var result2 = list.stream().skip(1).toArray();
		var result3 = list.stream().limit(2).toList();
		var result4 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.toList();
		var result5 = list.stream().map(String::trim).toList();
		var result6 = list.stream().distinct().toList();
		var result7 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.map(String::trim)
			.toArray();
	}
}

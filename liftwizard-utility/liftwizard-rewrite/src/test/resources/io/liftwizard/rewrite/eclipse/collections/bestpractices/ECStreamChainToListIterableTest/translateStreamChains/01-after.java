import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		var result1 = list.toList();
		var result2 = list.drop(1).toArray();
		var result3 = list.take(2);
		var result4 = list
			.select((each) -> !each.isEmpty());
		var result5 = list.collect(String::trim);
		var result6 = list.distinct();
		var result7 = list
			.select((each) -> !each.isEmpty())
			.collect(String::trim)
			.toArray();
	}
}

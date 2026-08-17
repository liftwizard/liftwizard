import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list, Predicate<String> predicate, long n)
	{
		var result1 = list.stream().collect(Collectors.groupingBy(String::length));
		var result2 = list.stream().count();
		var result3 = list.stream().toArray(String[]::new);
		var result4 = list.stream().filter(predicate).toList();
		var result5 = list.stream().skip(n).toArray();
		var result6 = list.stream().findFirst();
		var result7 = list.stream().skip(1);
		Stream<String> stream = list.stream();
	}
}

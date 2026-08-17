import java.util.List;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	void test(List<String> jcfList, MutableSet<String> set)
	{
		var result1 = jcfList.stream().skip(1).toArray();
		var result2 = set.stream().map(String::trim).toList();
		var result3 = set.stream().anyMatch(String::isEmpty);
	}
}

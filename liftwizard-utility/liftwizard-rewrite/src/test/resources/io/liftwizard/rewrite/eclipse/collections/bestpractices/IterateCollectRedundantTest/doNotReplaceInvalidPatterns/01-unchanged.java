import java.util.Collection;
import java.util.List;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void test(ImmutableList<String> immutableList, List<String> javaList, Iterable<String> iterable)
	{
		var result1 = Iterate.collect(immutableList, (s) -> s.toUpperCase());
		var result2 = Iterate.collect(javaList, String::length);
		var result3 = Iterate.collect(iterable, String::length);
		Collection<String> result4 = Iterate.collect(immutableList, (s) -> s.toUpperCase());
		Collection<Integer> result5 = Iterate.collect(javaList, String::length);
		Collection<Integer> result6 = Iterate.collect(iterable, String::length);
	}
}

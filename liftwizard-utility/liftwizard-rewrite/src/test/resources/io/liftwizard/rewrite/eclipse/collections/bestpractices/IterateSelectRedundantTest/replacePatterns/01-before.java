import java.util.Collection;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void test(MutableList<String> list, MutableSet<Integer> set, Predicate<String> predicate)
	{
		var result1 = Iterate.select(list, (s) -> s.length() > 5);
		var result2 = Iterate.select(list, predicate);
		var result3 = Iterate.select(list, String::isEmpty);
		var result4 = Iterate.select(set, (i) -> i > 0);
		Collection<String> result5 = Iterate.select(list, (s) -> s.length() > 5);
		Collection<String> result6 = Iterate.select(list, predicate);
		Collection<String> result7 = Iterate.select(list, String::isEmpty);
		Collection<Integer> result8 = Iterate.select(set, (i) -> i > 0);
	}
}

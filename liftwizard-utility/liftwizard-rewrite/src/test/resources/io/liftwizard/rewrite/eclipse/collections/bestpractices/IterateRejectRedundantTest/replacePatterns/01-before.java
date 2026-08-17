import java.util.Collection;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void test(MutableList<String> list, MutableSet<Integer> set, Predicate<String> predicate)
	{
		var result1 = Iterate.reject(list, (s) -> s.length() > 5);
		var result2 = Iterate.reject(list, predicate);
		var result3 = Iterate.reject(list, String::isEmpty);
		var result4 = Iterate.reject(set, (i) -> i > 0);
		Collection<String> result5 = Iterate.reject(list, (s) -> s.length() > 5);
		Collection<String> result6 = Iterate.reject(list, predicate);
		Collection<String> result7 = Iterate.reject(list, String::isEmpty);
		Collection<Integer> result8 = Iterate.reject(set, (i) -> i > 0);
	}
}

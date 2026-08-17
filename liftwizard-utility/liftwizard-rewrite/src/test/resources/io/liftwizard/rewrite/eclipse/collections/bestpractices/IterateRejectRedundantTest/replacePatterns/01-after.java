import java.util.Collection;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	void test(MutableList<String> list, MutableSet<Integer> set, Predicate<String> predicate)
	{
		var result1 = list.reject((s) -> s.length() > 5);
		var result2 = list.reject(predicate);
		var result3 = list.reject(String::isEmpty);
		var result4 = set.reject((i) -> i > 0);
		Collection<String> result5 = list.reject((s) -> s.length() > 5);
		Collection<String> result6 = list.reject(predicate);
		Collection<String> result7 = list.reject(String::isEmpty);
		Collection<Integer> result8 = set.reject((i) -> i > 0);
	}
}

import java.util.Collection;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	void test(MutableList<String> list, MutableSet<Integer> set, Predicate<String> predicate)
	{
		var result1 = list.select((s) -> s.length() > 5);
		var result2 = list.select(predicate);
		var result3 = list.select(String::isEmpty);
		var result4 = set.select((i) -> i > 0);
		Collection<String> result5 = list.select((s) -> s.length() > 5);
		Collection<String> result6 = list.select(predicate);
		Collection<String> result7 = list.select(String::isEmpty);
		Collection<Integer> result8 = set.select((i) -> i > 0);
	}
}

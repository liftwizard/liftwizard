import java.util.Collection;
import java.util.List;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.utility.Iterate;

class Test {
	void test(ImmutableList<String> immutableList, List<String> javaList, Iterable<String> iterable) {
		var result1 = Iterate.reject(immutableList, String::isEmpty);
		var result2 = Iterate.reject(javaList, s -> s.length() > 5);
		var result3 = Iterate.reject(iterable, s -> s.length() > 5);
		Collection<String> result4 = Iterate.reject(immutableList, String::isEmpty);
		Collection<String> result5 = Iterate.reject(javaList, s -> s.length() > 5);
		Collection<String> result6 = Iterate.reject(iterable, s -> s.length() > 5);
	}
}

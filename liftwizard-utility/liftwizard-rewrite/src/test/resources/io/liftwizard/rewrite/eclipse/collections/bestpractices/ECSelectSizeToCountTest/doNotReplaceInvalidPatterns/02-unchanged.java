import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class TestIntermediate {
	int test(MutableList<String> list, Predicate<String> predicate) {
		return list.select(predicate).collect(String::toUpperCase).size();
	}
}

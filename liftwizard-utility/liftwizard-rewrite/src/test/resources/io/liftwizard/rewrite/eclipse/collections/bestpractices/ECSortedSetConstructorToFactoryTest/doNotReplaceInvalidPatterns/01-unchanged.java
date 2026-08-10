import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

class Test {
	private final TreeSortedSet<String> fieldConcreteType = new TreeSortedSet<>();

	void test() {
		Set<Integer> regularSet = new HashSet<>();
		TreeSortedSet<Integer> concreteTypeEmpty = new TreeSortedSet<>();
		TreeSortedSet<Integer> concreteTypeComparator = new TreeSortedSet<>(Comparator.naturalOrder());
		TreeSortedSet<Integer> concreteTypeSet = new TreeSortedSet<>(regularSet);
	}
}

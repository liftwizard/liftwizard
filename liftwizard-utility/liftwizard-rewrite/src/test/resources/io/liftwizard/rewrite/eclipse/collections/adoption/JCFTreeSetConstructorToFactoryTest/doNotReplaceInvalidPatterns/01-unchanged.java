import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

class Test {
	private final TreeSet<String> fieldConcreteType = new TreeSet<>();

	// FieldAccess expressions - should not crash
	public static final Object INSTANCE = java.util.Collections.EMPTY_SET;
	public static final java.util.List<?> EMPTY_LIST = java.util.Collections.EMPTY_LIST;

	void test(Collection<String> inputCollection) {
		TreeSet<String> diamondSet = new TreeSet<>();
		TreeSet rawSet = new TreeSet();
		TreeSet<String> concreteFromCollection = new TreeSet<>(inputCollection);
		TreeSet<String> concreteWithComparator = new TreeSet<>(Comparator.naturalOrder());
	}
}

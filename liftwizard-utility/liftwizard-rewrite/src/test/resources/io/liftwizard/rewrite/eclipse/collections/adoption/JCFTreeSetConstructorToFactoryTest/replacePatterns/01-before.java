import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

class Test
{
	private final SortedSet<String> fieldInterfaceEmpty = new TreeSet<>();
	private final SortedSet<String> fieldInterfaceComparator = new TreeSet<>(Comparator.naturalOrder());
	private final SortedSet<String> fieldInterfaceCollection = new TreeSet<>(Arrays.asList("a", "b"));

	// FieldAccess expression - should be ignored without crashing
	public static final Object INSTANCE = java.util.Collections.EMPTY_SET;

	void test(Collection<String> inputCollection)
	{
		Collection<String> collection = new TreeSet<>();
		SortedSet<String> typeInference = new TreeSet<>();
		SortedSet<List<String>> nestedGenerics = new TreeSet<>();
		SortedSet<? extends Number> wildcardGenerics = new TreeSet<>();
		SortedSet<String> explicitSimple = new TreeSet<String>();
		SortedSet<List<String>> explicitNested = new TreeSet<List<String>>();
		java.util.SortedSet<String> fullyQualified = new TreeSet<>();
		SortedSet<String> interfaceWithComparator = new TreeSet<>(Comparator.naturalOrder());
		SortedSet<String> interfaceFromCollection = new TreeSet<>(inputCollection);
		SortedSet<String> fromList = new TreeSet<>(Arrays.asList("x", "y", "z"));
	}
}

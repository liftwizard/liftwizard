import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.collections.api.factory.SortedSets;

class Test
{
	private final SortedSet<String> fieldInterfaceEmpty = SortedSets.mutable.empty();
	private final SortedSet<String> fieldInterfaceComparator = SortedSets.mutable.with(Comparator.naturalOrder());
	private final SortedSet<String> fieldInterfaceCollection = SortedSets.mutable.withAll(Arrays.asList("a", "b"));

	// FieldAccess expression - should be ignored without crashing
	public static final Object INSTANCE = java.util.Collections.EMPTY_SET;

	void test(Collection<String> inputCollection)
	{
		Collection<String> collection = SortedSets.mutable.empty();
		SortedSet<String> typeInference = SortedSets.mutable.empty();
		SortedSet<List<String>> nestedGenerics = SortedSets.mutable.empty();
		SortedSet<? extends Number> wildcardGenerics = SortedSets.mutable.empty();
		SortedSet<String> explicitSimple = SortedSets.mutable.<String>empty();
		SortedSet<List<String>> explicitNested = SortedSets.mutable.<List<String>>empty();
		java.util.SortedSet<String> fullyQualified = SortedSets.mutable.empty();
		SortedSet<String> interfaceWithComparator = SortedSets.mutable.with(Comparator.naturalOrder());
		SortedSet<String> interfaceFromCollection = SortedSets.mutable.withAll(inputCollection);
		SortedSet<String> fromList = SortedSets.mutable.withAll(Arrays.asList("x", "y", "z"));
	}
}

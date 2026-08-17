import java.util.List;
import java.util.Set;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

class Test
{
	private final Set<String> fieldSet = Sets.mutable.empty();

	void test()
	{
		Set<String> simpleSet = Sets.mutable.empty();
		java.util.Set<String> fullyQualifiedSet = Sets.mutable.empty();
		Set rawSet = Sets.mutable.empty();
		java.util.Set rawSetFullyQualified = Sets.mutable.empty();
		Set<List<Integer>> nestedGenerics = Sets.mutable.empty();
		Set<String> unifiedSet = UnifiedSet.newSet();
		Set<String> set1 = Sets.mutable.empty(),
			set2 = Sets.mutable.with("a");
		Set<? extends Number> wildcardGenerics = Sets.mutable.empty();
	}

	/**
	 * Tests that {@link Set#size()} method works correctly.
	 * Also tests {@link List#size()} method.
	 */
	void javaDocReference()
	{
		Set<String> set = Sets.mutable.empty();
	}
}

class InstanceofExample
{
	void method()
	{
		Object obj = Sets.mutable.empty();
		if (obj instanceof Set)
		{
			Set<String> set = Sets.mutable.empty();
		}
	}
}

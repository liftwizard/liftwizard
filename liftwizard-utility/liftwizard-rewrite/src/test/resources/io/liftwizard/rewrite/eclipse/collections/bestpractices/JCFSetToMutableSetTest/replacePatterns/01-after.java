import java.util.List;
import java.util.Set;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

class Test
{
	private final MutableSet<String> fieldSet = Sets.mutable.empty();

	void test()
	{
		MutableSet<String> simpleSet = Sets.mutable.empty();
		MutableSet<String> fullyQualifiedSet = Sets.mutable.empty();
		MutableSet rawSet = Sets.mutable.empty();
		MutableSet rawSetFullyQualified = Sets.mutable.empty();
		MutableSet<List<Integer>> nestedGenerics = Sets.mutable.empty();
		MutableSet<String> unifiedSet = UnifiedSet.newSet();
		MutableSet<String> set1 = Sets.mutable.empty(),
			set2 = Sets.mutable.with("a");
		MutableSet<? extends Number> wildcardGenerics = Sets.mutable.empty();
	}

	/**
	 * Tests that {@link Set#size()} method works correctly.
	 * Also tests {@link List#size()} method.
	 */
	void javaDocReference()
	{
		MutableSet<String> set = Sets.mutable.empty();
	}
}

class InstanceofExample
{
	void method()
	{
		Object obj = Sets.mutable.empty();
		if (obj instanceof Set)
		{
			MutableSet<String> set = Sets.mutable.empty();
		}
	}
}

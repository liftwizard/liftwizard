import java.util.Collection;
import java.util.List;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.impl.list.mutable.FastList;

class Test
{
	private final Collection<String> fieldCollection = Lists.mutable.empty();

	void test()
	{
		Collection<String> simpleCollection = Lists.mutable.with("a", "b");
		java.util.Collection<String> fullyQualifiedCollection = Sets.mutable.empty();
		Collection rawCollection = Lists.mutable.empty();
		java.util.Collection fullyQualifiedRawCollection = Sets.mutable.empty();
		Collection<List<Integer>> nestedGenericsCollection = Lists.mutable.empty();
		Collection<String> fastList = FastList.newList();
		Collection<String> collection1 = Lists.mutable.empty(),
			collection2 = Sets.mutable.with("x");
		Collection<? extends Number> wildcardGenerics = Lists.mutable.empty();
	}

	/**
	 * Tests that {@link Collection#size()} method works correctly.
	 * Also tests {@link List#size()} method.
	 */
	void javaDocReference()
	{
		Collection<String> collection = Lists.mutable.empty();
	}
}

class InstanceofExample
{
	void method()
	{
		Object obj = Lists.mutable.empty();
		if (obj instanceof Collection)
		{
			Collection<String> collection = Sets.mutable.empty();
		}
	}
}

import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.impl.list.mutable.FastList;

class Test
{
	private final MutableCollection<String> fieldCollection = Lists.mutable.empty();

	void test()
	{
		MutableCollection<String> simpleCollection = Lists.mutable.with("a", "b");
		MutableCollection<String> fullyQualifiedCollection = Sets.mutable.empty();
		MutableCollection rawCollection = Lists.mutable.empty();
		MutableCollection fullyQualifiedRawCollection = Sets.mutable.empty();
		MutableCollection<List<Integer>> nestedGenericsCollection = Lists.mutable.empty();
		MutableCollection<String> fastList = FastList.newList();
		MutableCollection<String> collection1 = Lists.mutable.empty(),
			collection2 = Sets.mutable.with("x");
		MutableCollection<? extends Number> wildcardGenerics = Lists.mutable.empty();
	}

	/**
	 * Tests that {@link Collection#size()} method works correctly.
	 * Also tests {@link List#size()} method.
	 */
	void javaDocReference()
	{
		MutableCollection<String> collection = Lists.mutable.empty();
	}
}

class InstanceofExample
{
	void method()
	{
		Object obj = Lists.mutable.empty();
		if (obj instanceof Collection)
		{
			MutableCollection<String> collection = Sets.mutable.empty();
		}
	}
}

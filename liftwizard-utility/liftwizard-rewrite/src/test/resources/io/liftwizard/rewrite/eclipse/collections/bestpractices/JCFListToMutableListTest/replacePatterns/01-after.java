import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;

class Test
{
	private final MutableList<String> fieldList = Lists.mutable.empty();

	void test()
	{
		MutableList<String> simpleList = Lists.mutable.empty();
		MutableList<String> fullyQualifiedList = Lists.mutable.empty();
		MutableList rawList = Lists.mutable.empty();
		MutableList fullyQualifiedRawList = Lists.mutable.empty();
		MutableList<Map<String, Integer>> nestedGenericsList = Lists.mutable.empty();
		MutableList<String> fastList = FastList.newList();
		MutableList<String> list1 = Lists.mutable.empty(),
			list2 = Lists.mutable.with("a", "b");
		MutableList<? extends Number> wildcardGenerics = Lists.mutable.empty();
	}

	/**
	 * Tests that {@link Map#size()} method works correctly.
	 * Also tests {@link List#size()} method.
	 */
	void javaDocReference()
	{
		MutableList<String> list = Lists.mutable.empty();
	}
}

class InstanceofExample
{
	void method()
	{
		Object obj = Lists.mutable.empty();
		if (obj instanceof List)
		{
			MutableList<String> list = Lists.mutable.empty();
		}
	}
}

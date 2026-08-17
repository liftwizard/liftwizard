import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;

class Test
{
	private final List<String> fieldInterfaceEmpty = Lists.mutable.empty();
	private final List<Integer> fieldInterfaceCapacity = Lists.mutable.withInitialCapacity(10);
	private final List<String> fieldInterfaceCollection = Lists.mutable.withAll(Arrays.asList("a", "b"));

	void test(Collection<String> inputCollection)
	{
		Collection<String> collection = Lists.mutable.empty();
		List<String> typeInference = Lists.mutable.empty();
		List<List<String>> nestedGenerics = Lists.mutable.empty();
		List<? extends Number> wildcardGenerics = Lists.mutable.empty();
		List<String> explicitSimple = Lists.mutable.<String>empty();
		List<List<String>> explicitNested = Lists.mutable.<List<String>>empty();
		java.util.List<String> fullyQualified = Lists.mutable.empty();
		List<String> withCapacity20 = Lists.mutable.withInitialCapacity(20);
		List<String> explicit30 = Lists.mutable.<String>withInitialCapacity(30);
		List<String> interfaceFromCollection = Lists.mutable.withAll(inputCollection);
		List<String> fromList = Lists.mutable.withAll(Arrays.asList("x", "y", "z"));
	}
}

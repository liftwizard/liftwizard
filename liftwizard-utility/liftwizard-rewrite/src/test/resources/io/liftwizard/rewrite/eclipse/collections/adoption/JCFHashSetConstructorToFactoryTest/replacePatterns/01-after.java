import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.factory.Sets;

class Test
{
	private final Set<String> fieldInterfaceEmpty = Sets.mutable.empty();
	private final Set<Integer> fieldInterfaceCapacity = Sets.mutable.withInitialCapacity(10);
	private final Set<String> fieldInterfaceCollection = Sets.mutable.withAll(Arrays.asList("a", "b"));

	void test(Collection<String> inputCollection)
	{
		Collection<String> collection = Sets.mutable.empty();
		Set<String> typeInference = Sets.mutable.empty();
		Set<List<String>> nestedGenerics = Sets.mutable.empty();
		Set<? extends Number> wildcardGenerics = Sets.mutable.empty();
		Set<String> explicitSimple = Sets.mutable.<String>empty();
		Set<List<String>> explicitNested = Sets.mutable.<List<String>>empty();
		java.util.Set<String> fullyQualified = Sets.mutable.empty();
		Set<String> withCapacity20 = Sets.mutable.withInitialCapacity(20);
		Set<String> explicit30 = Sets.mutable.<String>withInitialCapacity(30);
		Set<String> interfaceFromCollection = Sets.mutable.withAll(inputCollection);
		Set<String> fromList = Sets.mutable.withAll(Arrays.asList("x", "y", "z"));
	}
}

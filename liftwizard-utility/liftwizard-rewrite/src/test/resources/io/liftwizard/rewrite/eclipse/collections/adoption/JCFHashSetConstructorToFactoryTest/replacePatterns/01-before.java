import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Test
{
	private final Set<String> fieldInterfaceEmpty = new HashSet<>();
	private final Set<Integer> fieldInterfaceCapacity = new HashSet<>(10);
	private final Set<String> fieldInterfaceCollection = new HashSet<>(Arrays.asList("a", "b"));

	void test(Collection<String> inputCollection)
	{
		Collection<String> collection = new HashSet<>();
		Set<String> typeInference = new HashSet<>();
		Set<List<String>> nestedGenerics = new HashSet<>();
		Set<? extends Number> wildcardGenerics = new HashSet<>();
		Set<String> explicitSimple = new HashSet<String>();
		Set<List<String>> explicitNested = new HashSet<List<String>>();
		java.util.Set<String> fullyQualified = new HashSet<>();
		Set<String> withCapacity20 = new HashSet<>(20);
		Set<String> explicit30 = new HashSet<String>(30);
		Set<String> interfaceFromCollection = new HashSet<>(inputCollection);
		Set<String> fromList = new HashSet<>(Arrays.asList("x", "y", "z"));
	}
}

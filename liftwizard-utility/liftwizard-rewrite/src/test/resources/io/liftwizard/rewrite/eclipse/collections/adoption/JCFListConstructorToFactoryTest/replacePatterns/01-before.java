import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class Test
{
	private final List<String> fieldInterfaceEmpty = new ArrayList<>();
	private final List<Integer> fieldInterfaceCapacity = new ArrayList<>(10);
	private final List<String> fieldInterfaceCollection = new ArrayList<>(Arrays.asList("a", "b"));

	void test(Collection<String> inputCollection)
	{
		Collection<String> collection = new ArrayList<>();
		List<String> typeInference = new ArrayList<>();
		List<List<String>> nestedGenerics = new ArrayList<>();
		List<? extends Number> wildcardGenerics = new ArrayList<>();
		List<String> explicitSimple = new ArrayList<String>();
		List<List<String>> explicitNested = new ArrayList<List<String>>();
		java.util.List<String> fullyQualified = new ArrayList<>();
		List<String> withCapacity20 = new ArrayList<>(20);
		List<String> explicit30 = new ArrayList<String>(30);
		List<String> interfaceFromCollection = new ArrayList<>(inputCollection);
		List<String> fromList = new ArrayList<>(Arrays.asList("x", "y", "z"));
	}
}

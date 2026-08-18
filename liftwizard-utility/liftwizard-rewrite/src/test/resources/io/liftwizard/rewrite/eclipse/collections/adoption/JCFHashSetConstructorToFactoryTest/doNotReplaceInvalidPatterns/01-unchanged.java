import java.util.Collection;
import java.util.HashSet;

class Test
{
	private final HashSet<String> fieldConcreteType = new HashSet<>();

	void test(Collection<String> inputCollection)
	{
		HashSet<String> diamondSet = new HashSet<>();
		HashSet rawSet = new HashSet();
		HashSet<String> withInitialCapacity = new HashSet<>(10);
		HashSet<String> concreteFromCollection = new HashSet<>(inputCollection);
	}
}

import java.util.ArrayList;
import java.util.Collection;

class Test {
	private final ArrayList<String> fieldConcreteType = new ArrayList<>();

	void test(Collection<String> inputCollection) {
		ArrayList<String> diamondList = new ArrayList<>();
		ArrayList rawList = new ArrayList();
		ArrayList<String> withInitialCapacity = new ArrayList<>(10);
		ArrayList<String> concreteFromCollection = new ArrayList<>(inputCollection);
	}
}

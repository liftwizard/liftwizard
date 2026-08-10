import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import java.util.HashMap;

class Test {
	private final UnifiedMap<String, Integer> fieldConcreteType = new UnifiedMap<>();
	private final UnifiedMap<String, Integer> fieldConcreteCapacity = new UnifiedMap<>(10);
	private final UnifiedMap<String, Integer> fieldConcreteMap = new UnifiedMap<>(this.fieldConcreteType);

	void test() {
		UnifiedMap<String, Integer> concreteTypeEmpty = new UnifiedMap<>();
		UnifiedMap<String, Integer> concreteTypeCapacity = new UnifiedMap<>(10);
		UnifiedMap<String, Integer> concreteTypeMap = new UnifiedMap<>(concreteTypeEmpty);
		HashMap<String, Integer> jdkMap = new HashMap<>();
	}
}

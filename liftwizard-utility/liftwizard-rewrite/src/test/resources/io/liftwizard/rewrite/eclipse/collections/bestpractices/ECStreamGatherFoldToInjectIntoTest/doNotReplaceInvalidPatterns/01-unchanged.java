import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	ArrayList<Integer> arrayList;
	MutableList<Integer> mutableList;
	Supplier<Integer> supplier = () -> 0;

	Integer nonEclipseCollectionsType()
	{
		return arrayList
			.stream()
			.gather(Gatherers.fold(() -> 0, Integer::sum))
			.findFirst()
			.orElseThrow();
	}

	Stream<Integer> withoutFindFirst()
	{
		return mutableList.stream().gather(Gatherers.fold(() -> 0, Integer::sum));
	}

	Integer nonSupplierLambda()
	{
		return mutableList.stream().gather(Gatherers.fold(supplier, Integer::sum)).findFirst().orElseThrow();
	}
}

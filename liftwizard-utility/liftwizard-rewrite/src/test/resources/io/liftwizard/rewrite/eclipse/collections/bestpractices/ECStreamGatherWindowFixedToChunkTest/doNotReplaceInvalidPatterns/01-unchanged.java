import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	ArrayList<String> arrayList;
	MutableList<String> mutableList;

	List<List<String>> nonEclipseCollectionsType()
	{
		return arrayList.stream().gather(Gatherers.windowFixed(3)).collect(Collectors.toList());
	}

	Stream<List<String>> withoutCollect()
	{
		return mutableList.stream().gather(Gatherers.windowFixed(3));
	}
}

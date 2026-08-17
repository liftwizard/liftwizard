import java.util.ArrayList;
import java.util.OptionalDouble;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	ArrayList<String> arrayList = new ArrayList<>();
	MutableList<String> mutableList;

	double nonEclipseCollectionsType = arrayList.stream().mapToDouble(String::length).sum();

	OptionalDouble mapToDoubleWithoutSum = mutableList.stream().mapToDouble(String::length).average();
}

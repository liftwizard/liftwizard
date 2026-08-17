import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.IntList;

class Test
{
	String testOtherDetectCalls(MutableList<String> list)
	{
		String result = list.detect((s) -> s.length() > 5);
		return result != null ? result : "default";
	}

	boolean testPrimitiveLists(IntList list)
	{
		return list.detectIfNone((i) -> i > 5, -1) != -1;
	}
}

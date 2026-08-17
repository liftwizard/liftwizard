import org.eclipse.collections.api.list.MutableList;

class TestLambda
{
	int test(MutableList<String> list)
	{
		return list.count((s) -> s.length() > 5);
	}
}

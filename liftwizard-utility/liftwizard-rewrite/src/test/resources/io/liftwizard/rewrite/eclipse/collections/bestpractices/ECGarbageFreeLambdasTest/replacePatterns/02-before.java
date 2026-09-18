import org.eclipse.collections.api.list.MutableList;

class TestMultiLineChain
{
	MutableList<String> strings;
	String prefix;
	String suffix;

	void all()
	{
		strings
			.asLazy()
			.select((s) -> s.startsWith(prefix))
			.collect((s) -> s.concat(suffix))
			.toList();
	}
}

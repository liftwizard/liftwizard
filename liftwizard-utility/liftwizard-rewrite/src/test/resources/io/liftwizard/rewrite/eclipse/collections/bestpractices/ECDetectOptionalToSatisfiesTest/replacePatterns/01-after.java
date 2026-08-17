import org.eclipse.collections.api.list.MutableList;

class Test
{
	void example(MutableList<String> list)
	{
		boolean detectOptionalIsPresent = list.anySatisfy((s) -> s.length() > 5);
		boolean negatedDetectOptionalIsPresent = list.noneSatisfy((s) -> s.length() > 5);
		boolean detectOptionalIsEmpty = list.noneSatisfy((s) -> s.length() > 5);
		boolean negatedDetectOptionalIsEmpty = list.anySatisfy((s) -> s.length() > 5);
	}
}

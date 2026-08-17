import org.eclipse.collections.api.list.MutableList;

class Test
{
	void example(MutableList<String> list)
	{
		boolean detectOptionalIsPresent = list.detectOptional((s) -> s.length() > 5).isPresent();
		boolean negatedDetectOptionalIsPresent = !list.detectOptional((s) -> s.length() > 5).isPresent();
		boolean detectOptionalIsEmpty = list.detectOptional((s) -> s.length() > 5).isEmpty();
		boolean negatedDetectOptionalIsEmpty = !list.detectOptional((s) -> s.length() > 5).isEmpty();
	}
}

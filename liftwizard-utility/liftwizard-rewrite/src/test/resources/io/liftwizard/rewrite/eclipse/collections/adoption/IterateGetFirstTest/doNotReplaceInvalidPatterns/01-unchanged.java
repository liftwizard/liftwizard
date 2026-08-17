import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class Test
{
	void test(List<String> list)
	{
		Iterator<String> iter = list.iterator();
		String first = iter.next();

		ListIterator<String> listIter = list.listIterator();
		String second = listIter.next();

		iter.hasNext();
		listIter.hasPrevious();
	}
}

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> stringList, MutableList<Integer> intList, Predicate<String> predicate)
	{
		// non-negated lambdas should not change
		MutableList<String> selectResult = stringList.select((s) -> s.isEmpty());
		MutableList<String> rejectResult = stringList.reject((s) -> s.isEmpty());
		MutableList<String> lengthCheck = stringList.select((s) -> s.length() > 5);

		// method references should not change
		MutableList<String> selectWithMethodRef = stringList.select(String::isEmpty);
		MutableList<String> selectWithPredicate = stringList.select(predicate);

		// == comparison should not change
		MutableList<Integer> equalComparison = intList.select((n) -> n == 0);

		// other comparisons should not change
		MutableList<Integer> lt = intList.select((n) -> n < 5);
		MutableList<Integer> gt = intList.select((n) -> n > 5);
		MutableList<Integer> le = intList.select((n) -> n <= 5);
		MutableList<Integer> ge = intList.select((n) -> n >= 5);
	}
}

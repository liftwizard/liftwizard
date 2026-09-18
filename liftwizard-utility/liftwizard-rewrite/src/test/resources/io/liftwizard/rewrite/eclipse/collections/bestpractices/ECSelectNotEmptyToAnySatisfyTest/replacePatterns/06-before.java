import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class TestMultiLineChain
{
	boolean test(
		MutableList<String> candidateNames,
		Predicate<String> isExcludedFromSearchResults,
		Predicate<String> isEligibleForPromotion
	)
	{
		boolean result = candidateNames
			.asLazy()
			.reject(isExcludedFromSearchResults)
			.select(isEligibleForPromotion)
			.notEmpty();
		return result;
	}
}

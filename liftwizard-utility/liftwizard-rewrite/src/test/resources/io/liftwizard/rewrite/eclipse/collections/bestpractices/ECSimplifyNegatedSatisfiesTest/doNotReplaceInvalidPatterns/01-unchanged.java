import java.util.function.Predicate;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void nonNegatedCalls(MutableList<String> list)
	{
		boolean nonNegatedAnySatisfy = list.anySatisfy((s) -> s.length() > 5);
		boolean nonNegatedNoneSatisfy = list.noneSatisfy((s) -> s.isEmpty());
		boolean combined = list.anySatisfy((s) -> s.length() > 5) || list.noneSatisfy((s) -> s.isEmpty());
	}

	class NoneSatisfyImplementation
		implements RichIterable<String>
	{
		@Override
		public boolean noneSatisfy(Predicate<? super String> predicate)
		{
			return !this.anySatisfy(predicate);
		}

		@Override
		public boolean anySatisfy(Predicate<? super String> predicate)
		{
			return false;
		}
	}

	class AnySatisfyImplementation
		implements RichIterable<String>
	{
		@Override
		public boolean anySatisfy(Predicate<? super String> predicate)
		{
			return !this.noneSatisfy(predicate);
		}

		@Override
		public boolean noneSatisfy(Predicate<? super String> predicate)
		{
			return true;
		}
	}

	class CircularImplementation
		implements RichIterable<String>
	{
		@Override
		public boolean noneSatisfy(Predicate<? super String> predicate)
		{
			return !this.anySatisfy(predicate);
		}

		@Override
		public boolean anySatisfy(Predicate<? super String> predicate)
		{
			return !this.noneSatisfy(predicate);
		}
	}
}

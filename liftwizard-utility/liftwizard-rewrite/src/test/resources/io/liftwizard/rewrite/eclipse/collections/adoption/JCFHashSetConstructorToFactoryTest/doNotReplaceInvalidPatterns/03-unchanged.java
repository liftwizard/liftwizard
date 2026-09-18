package example;

import java.util.HashSet;
import java.util.Set;
import other.Sets;

class UnrelatedSetsAlreadyImported
{
	private final Sets sets = new Sets();

	void test()
	{
		Set<String> visited = new HashSet<>();
	}
}

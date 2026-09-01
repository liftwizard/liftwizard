package example;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

class ImplFactorySetsAlreadyImported
{
	void test(MutableSet<String> left, MutableSet<String> right)
	{
		MutableSet<String> union = Sets.union(left, right);
		Set<String> visited = new HashSet<>();
	}
}

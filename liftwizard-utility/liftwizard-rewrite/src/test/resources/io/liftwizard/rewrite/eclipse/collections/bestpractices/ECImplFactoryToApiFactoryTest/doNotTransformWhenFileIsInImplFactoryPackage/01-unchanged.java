package org.eclipse.collections.impl.factory;

import java.util.Set;

public class SetsTest {
	void factoryUsage() {
		var set = Sets.mutable.empty();
	}

	void utilityUsage(Set<String> a, Set<String> b) {
		Set<String> union = Sets.union(a, b);
	}
}

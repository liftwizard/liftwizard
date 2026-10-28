package org.eclipse.collections.impl.factory;

import java.util.SortedSet;
import java.util.TreeSet;

public final class Sets
{
	private Sets()
	{
	}

	public static <E> SortedSet<E> newSortedSet()
	{
		return new TreeSet<>();
	}
}

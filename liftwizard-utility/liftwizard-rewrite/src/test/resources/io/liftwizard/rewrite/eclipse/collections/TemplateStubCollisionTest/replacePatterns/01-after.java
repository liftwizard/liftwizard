package org.eclipse.collections.impl.factory;

import java.util.SortedSet;

import org.eclipse.collections.api.factory.SortedSets;

public final class Sets
{
	private Sets()
	{
	}

	public static <E> SortedSet<E> newSortedSet()
	{
		return SortedSets.mutable.empty();
	}
}

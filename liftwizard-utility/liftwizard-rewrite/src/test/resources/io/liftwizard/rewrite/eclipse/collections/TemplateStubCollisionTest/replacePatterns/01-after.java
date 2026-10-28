package org.eclipse.collections.impl.factory;

import java.util.List;
import java.util.SortedSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.MutableList;

public final class Sets
{
	private Sets()
	{
	}

	public static <E> SortedSet<E> newSortedSet()
	{
		return SortedSets.mutable.empty();
	}

	public static <E> List<E> newEmptyList()
	{
		return Lists.fixedSize.empty();
	}

	public static MutableList<String> newListWith()
	{
		return Lists.mutable.with("a", "b");
	}

	public static <E> MutableList<E> newMutableList()
	{
		MutableList<E> list = Lists.mutable.empty();
		return list;
	}
}

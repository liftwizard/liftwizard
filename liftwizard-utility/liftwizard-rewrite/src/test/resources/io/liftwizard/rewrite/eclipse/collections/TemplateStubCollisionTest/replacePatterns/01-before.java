package org.eclipse.collections.impl.factory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;

public final class Sets
{
	private Sets()
	{
	}

	public static <E> SortedSet<E> newSortedSet()
	{
		return new TreeSet<>();
	}

	public static <E> List<E> newEmptyList()
	{
		return Collections.emptyList();
	}

	public static MutableList<String> newListWith()
	{
		return FastList.newList(Arrays.asList("a", "b"));
	}

	public static <E> MutableList<E> newMutableList()
	{
		MutableList<E> list = new FastList<>();
		return list;
	}
}

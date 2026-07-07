/*
 * Copyright 2026 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.rewrite.eclipse.collections;

public final class EclipseCollectionsTemplateStubs {

	public static final String[] FACTORIES = {
		"""
			package org.eclipse.collections.api.factory;

			public final class Bags {
			    public static final Mutable mutable = new Mutable();

			    public static final class Mutable {
			        @SafeVarargs
			        public final <T> org.eclipse.collections.api.bag.MutableBag<T> with(T... elements) {
			            return null;
			        }
			    }
			}
			""",
		"""
			package org.eclipse.collections.api.bag;

			public interface MutableBag<T> extends java.util.Collection<T> {
			}
			""",
		"""
			package org.eclipse.collections.api.factory;

			public final class Lists {
			    public static final FixedSize fixedSize = new FixedSize();
			    public static final Mutable mutable = new Mutable();

			    public static final class FixedSize {
			        public <T> org.eclipse.collections.api.list.MutableList<T> empty() {
			            return null;
			        }
			    }

			    public static final class Mutable {
			        public <T> org.eclipse.collections.api.list.MutableList<T> empty() {
			            return null;
			        }

			        @SafeVarargs
			        public final <T> org.eclipse.collections.api.list.MutableList<T> with(T... elements) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.list.MutableList<T> withAll(
			            java.lang.Iterable<? extends T> iterable
			        ) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.list.MutableList<T> withInitialCapacity(int capacity) {
			            return null;
			        }
			    }
			}
			""",
		"""
			package org.eclipse.collections.api.list;

			public interface MutableList<T> extends java.util.List<T> {
			}
			""",
		"""
			package org.eclipse.collections.api.factory;

			public final class Maps {
			    public static final FixedSize fixedSize = new FixedSize();
			    public static final Mutable mutable = new Mutable();

			    public static final class FixedSize {
			        public <K, V> org.eclipse.collections.api.map.MutableMap<K, V> empty() {
			            return null;
			        }
			    }

			    public static final class Mutable {
			        public <K, V> org.eclipse.collections.api.map.MutableMap<K, V> empty() {
			            return null;
			        }

			        public <K, V> org.eclipse.collections.api.map.MutableMap<K, V> withInitialCapacity(int capacity) {
			            return null;
			        }

			        public <K, V> org.eclipse.collections.api.map.MutableMap<K, V> withMap(
			            java.util.Map<? extends K, ? extends V> map
			        ) {
			            return null;
			        }
			    }
			}
			""",
		"""
			package org.eclipse.collections.api.map;

			public interface MutableMap<K, V> extends java.util.Map<K, V> {
			}
			""",
		"""
			package org.eclipse.collections.api.factory;

			public final class Sets {
			    public static final FixedSize fixedSize = new FixedSize();
			    public static final Mutable mutable = new Mutable();

			    public static final class FixedSize {
			        public <T> org.eclipse.collections.api.set.MutableSet<T> empty() {
			            return null;
			        }
			    }

			    public static final class Mutable {
			        public <T> org.eclipse.collections.api.set.MutableSet<T> empty() {
			            return null;
			        }

			        @SafeVarargs
			        public final <T> org.eclipse.collections.api.set.MutableSet<T> with(T... elements) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.set.MutableSet<T> withAll(
			            java.lang.Iterable<? extends T> iterable
			        ) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.set.MutableSet<T> withInitialCapacity(int capacity) {
			            return null;
			        }
			    }
			}
			""",
		"""
			package org.eclipse.collections.api.set;

			public interface MutableSet<T> extends java.util.Set<T> {
			}
			""",
		"""
			package org.eclipse.collections.api.factory;

			public final class SortedBags {
			    public static final Mutable mutable = new Mutable();

			    public static final class Mutable {
			        @SafeVarargs
			        public final <T> org.eclipse.collections.api.bag.sorted.MutableSortedBag<T> with(T... elements) {
			            return null;
			        }

			        @SafeVarargs
			        public final <T> org.eclipse.collections.api.bag.sorted.MutableSortedBag<T> with(
			            java.util.Comparator<? super T> comparator,
			            T... elements
			        ) {
			            return null;
			        }
			    }
			}
			""",
		"""
			package org.eclipse.collections.api.bag.sorted;

			public interface MutableSortedBag<T> extends java.util.Collection<T> {
			}
			""",
		"""
			package org.eclipse.collections.api.factory;

			public final class SortedMaps {
			    public static final Mutable mutable = new Mutable();

			    public static final class Mutable {
			        public <K, V> org.eclipse.collections.api.map.sorted.MutableSortedMap<K, V> empty() {
			            return null;
			        }

			        public <K, V> org.eclipse.collections.api.map.sorted.MutableSortedMap<K, V> with(
			            java.util.Comparator<? super K> comparator
			        ) {
			            return null;
			        }

			        public <K, V> org.eclipse.collections.api.map.sorted.MutableSortedMap<K, V> withAll(
			            java.util.Comparator<? super K> comparator,
			            java.lang.Iterable<?> iterable
			        ) {
			            return null;
			        }

			        public <K, V> org.eclipse.collections.api.map.sorted.MutableSortedMap<K, V> withInitialCapacity(
			            int capacity
			        ) {
			            return null;
			        }

			        public <K, V> org.eclipse.collections.api.map.sorted.MutableSortedMap<K, V> withSortedMap(
			            java.util.Map<? extends K, ? extends V> map
			        ) {
			            return null;
			        }
			    }
			}
			""",
		"""
			package org.eclipse.collections.api.map.sorted;

			public interface MutableSortedMap<K, V> extends java.util.SortedMap<K, V> {
			}
			""",
		"""
			package org.eclipse.collections.api.factory;

			public final class SortedSets {
			    public static final Mutable mutable = new Mutable();

			    public static final class Mutable {
			        public <T> org.eclipse.collections.api.set.sorted.MutableSortedSet<T> empty() {
			            return null;
			        }

			        @SafeVarargs
			        public final <T> org.eclipse.collections.api.set.sorted.MutableSortedSet<T> with(T... elements) {
			            return null;
			        }

			        @SafeVarargs
			        public final <T> org.eclipse.collections.api.set.sorted.MutableSortedSet<T> with(
			            java.util.Comparator<? super T> comparator,
			            T... elements
			        ) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.set.sorted.MutableSortedSet<T> with(
			            java.util.Comparator<? super T> comparator
			        ) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.set.sorted.MutableSortedSet<T> withAll(
			            java.util.Comparator<? super T> comparator,
			            java.lang.Iterable<? extends T> iterable
			        ) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.set.sorted.MutableSortedSet<T> withAll(
			            java.lang.Iterable<? extends T> iterable
			        ) {
			            return null;
			        }

			        public <T> org.eclipse.collections.api.set.sorted.MutableSortedSet<T> withInitialCapacity(int capacity) {
			            return null;
			        }
			    }
			}
			""",
		"""
			package org.eclipse.collections.api.set.sorted;

			public interface MutableSortedSet<T> extends java.util.SortedSet<T> {
			}
			""",
	};

	public static final String[] RICH_ITERABLE = {
		"""
			package org.eclipse.collections.api.block.function;

			@FunctionalInterface
			public interface Function0<R> {
			    R value();
			}
			""",
		"""
			package org.eclipse.collections.api.block.function;

			@FunctionalInterface
			public interface Function2<T, P, R> {
			    R value(T each, P parameter);
			}
			""",
		"""
			package org.eclipse.collections.api.block.predicate;

			@FunctionalInterface
			public interface Predicate2<T, P> {
			    boolean accept(T each, P parameter);
			}
			""",
		"""
			package org.eclipse.collections.api.block.procedure;

			@FunctionalInterface
			public interface Procedure2<T, P> {
			    void value(T each, P parameter);
			}
			""",
		"""
			package org.eclipse.collections.api;

			public interface RichIterable<T> {
			    <P> boolean anySatisfyWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P> boolean allSatisfyWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P, V> RichIterable<V> collectWith(
			        org.eclipse.collections.api.block.function.Function2<T, P, V> function,
			        P parameter
			    );

			    boolean contains(Object value);

			    <P> int countWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P, V> RichIterable<V> countByWith(
			        org.eclipse.collections.api.block.function.Function2<T, P, V> function,
			        P parameter
			    );

			    <P> T detectWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P> T detectWithIfNone(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter,
			        org.eclipse.collections.api.block.function.Function0<? extends T> function
			    );

			    <P> java.util.Optional<T> detectWithOptional(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P> void forEachWith(
			        org.eclipse.collections.api.block.procedure.Procedure2<T, P> procedure,
			        P parameter
			    );

			    <P> boolean noneSatisfyWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P> RichIterable<T> partitionWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P> RichIterable<T> rejectWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P> boolean removeIfWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );

			    <P> RichIterable<T> selectWith(
			        org.eclipse.collections.api.block.predicate.Predicate2<T, P> predicate,
			        P parameter
			    );
			}
			""",
	};

	private EclipseCollectionsTemplateStubs() {}
}

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

package io.liftwizard.rewrite.eclipse.collections.bestpractices;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class ECGarbageFreeLambdasTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipeFromResources("io.liftwizard.rewrite.eclipse.collections.bestpractices.ECGarbageFreeLambdas");
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Map;
					import java.util.Map.Entry;
					import org.eclipse.collections.api.RichIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> strings;
					    MutableList<StringBuilder> builders;
					    MutableList<Entry<String, Integer>> entries;
					    RichIterable<String> richIterable;
					    String prefix;
					    String target;
					    String suffix;
					    String text;
					    Integer value;

					    void all() {
					        strings.select(s -> s.startsWith(prefix));
					        strings.select(s -> s.equals(target));
					        strings.select(s -> { return s.startsWith(prefix); });
					        richIterable.select(s -> s.startsWith(prefix));
					        strings.reject(s -> s.startsWith(prefix));
					        strings.collect(s -> s.concat(suffix));
					        strings.detect(s -> s.startsWith(prefix));
					        strings.detectOptional(s -> s.startsWith(prefix));
					        strings.detectIfNone(s -> s.startsWith(prefix), () -> "fallback");
					        strings.anySatisfy(s -> s.startsWith(prefix));
					        strings.allSatisfy(s -> s.startsWith(prefix));
					        strings.noneSatisfy(s -> s.startsWith(prefix));
					        strings.count(s -> s.startsWith(prefix));
					        strings.partition(s -> s.startsWith(prefix));
					        strings.countBy(s -> s.concat(suffix));
					        builders.forEach(b -> b.append(text));
					        strings.removeIf(s -> s.startsWith(prefix));
					        entries.anySatisfy(e -> e.equals(value));
					        entries.forEach(e -> e.setValue(value));
					    }
					}
					""",
					"""
					import java.util.Map;
					import java.util.Map.Entry;
					import org.eclipse.collections.api.RichIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> strings;
					    MutableList<StringBuilder> builders;
					    MutableList<Entry<String, Integer>> entries;
					    RichIterable<String> richIterable;
					    String prefix;
					    String target;
					    String suffix;
					    String text;
					    Integer value;

					    void all() {
					        strings.selectWith(String::startsWith, prefix);
					        strings.selectWith(Object::equals, target);
					        strings.selectWith(String::startsWith, prefix);
					        richIterable.selectWith(String::startsWith, prefix);
					        strings.rejectWith(String::startsWith, prefix);
					        strings.collectWith(String::concat, suffix);
					        strings.detectWith(String::startsWith, prefix);
					        strings.detectWithOptional(String::startsWith, prefix);
					        strings.detectWithIfNone(String::startsWith, prefix, () -> "fallback");
					        strings.anySatisfyWith(String::startsWith, prefix);
					        strings.allSatisfyWith(String::startsWith, prefix);
					        strings.noneSatisfyWith(String::startsWith, prefix);
					        strings.countWith(String::startsWith, prefix);
					        strings.partitionWith(String::startsWith, prefix);
					        strings.countByWith(String::concat, suffix);
					        builders.forEachWith(StringBuilder::append, text);
					        strings.removeIfWith(String::startsWith, prefix);
					        entries.anySatisfyWith(Object::equals, value);
					        entries.forEachWith(Entry::setValue, value);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> strings;
					    MutableList<StringBuilder> builders;
					    Predicate<String> predicate;
					    Function<String, String> fn;
					    String prefix;

					    void all() {
					        strings.select(s -> s.startsWith(s));
					        strings.select(s -> s.isEmpty());
					        strings.select(s -> s.trim().startsWith(prefix));
					        strings.select(String::isEmpty);
					        strings.select(predicate);
					        strings.select(s -> s.regionMatches(0, prefix, 0, 1));
					        strings.collect(fn);
					        strings.collect(String::trim);
					        strings.detectIfNone(s -> s.startsWith(s), () -> "");
					        strings.detectIfNone(String::isEmpty, () -> "");
					        builders.forEach(b -> b.append(b.toString()));
					        builders.forEach(StringBuilder::reverse);
					    }
					}
					"""
				)
			);
	}
}

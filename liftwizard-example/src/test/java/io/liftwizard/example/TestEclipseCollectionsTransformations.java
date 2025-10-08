/*
 * Copyright 2025 Craig Motlin
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

package io.liftwizard.example;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.utility.Iterate;
import org.eclipse.collections.impl.utility.MapIterate;

/**
 * Test class to demonstrate Eclipse Collections transformations.
 * This class contains various patterns that should be transformed by OpenRewrite recipes.
 */
public class TestEclipseCollectionsTransformations {

    // Type declaration transformations
    public void testTypeDeclarations() {
        // Should transform to MutableList
        MutableList<String> list = Lists.mutable.empty();
        MutableList rawList = Lists.mutable.empty();

        // Should transform to MutableMap
        MutableMap<String, Integer> map = Maps.mutable.empty();
        MutableMap rawMap = Maps.mutable.empty();

        // Should transform to MutableSet
        MutableSet<String> set = Sets.mutable.empty();
        MutableSet rawSet = Sets.mutable.empty();
    }

    // Negated empty checks
    public void testNegatedEmptyChecks(RichIterable<String> richIterable) {
        // Should transform to notEmpty()
        if (richIterable.notEmpty()) {
            System.out.println("Not empty");
        }

        // Should transform to isEmpty()
        if (richIterable.isEmpty()) {
            System.out.println("Is empty");
        }
    }

    // Negated satisfies methods
    public void testNegatedSatisfies(RichIterable<String> richIterable, Predicate<String> predicate) {
        // Should transform to anySatisfy()
        if (richIterable.anySatisfy(predicate)) {
            System.out.println("At least one satisfies");
        }

        // Should transform to noneSatisfy()
        if (richIterable.noneSatisfy(predicate)) {
            System.out.println("None satisfy");
        }
    }

    // Detect optional to satisfies
    public void testDetectOptionalToSatisfies(MutableList<String> list, Predicate<String> predicate) {
        // Should transform to anySatisfy()
        if (list.anySatisfy(predicate)) {
            System.out.println("Found match");
        }

        // Should transform to noneSatisfy()
        if (list.noneSatisfy(predicate)) {
            System.out.println("No match found");
        }
    }

    // Count to satisfies
    public void testCountToSatisfies(MutableList<String> list, Predicate<String> predicate) {
        // Should transform to noneSatisfy()
        if (list.noneSatisfy(predicate)) {
            System.out.println("None match");
        }

        // Should transform to anySatisfy()
        if (list.anySatisfy(predicate)) {
            System.out.println("At least one matches");
        }

        // Should transform to anySatisfy()
        if (list.anySatisfy(predicate)) {
            System.out.println("At least one matches");
        }
    }

    // Size to empty
    public void testSizeToEmpty(MutableList<String> list) {
        // Should transform to isEmpty()
        if (list.isEmpty()) {
            System.out.println("Empty");
        }

        // Should transform to notEmpty()
        if (list.notEmpty()) {
            System.out.println("Not empty");
        }

        // Should transform to notEmpty()
        if (list.notEmpty()) {
            System.out.println("Not empty");
        }

        // Should transform to notEmpty()
        if (list.notEmpty()) {
            System.out.println("Not empty");
        }

        // Should transform to isEmpty()
        if (list.isEmpty()) {
            System.out.println("Empty");
        }

        // Should transform to isEmpty()
        if (list.isEmpty()) {
            System.out.println("Empty");
        }
    }

    // Iterate utility methods
    public void testIterateUtilityMethods(Iterable<String> iterable) {
        // Should transform to notEmpty()
        if (Iterate.notEmpty(iterable)) {
            System.out.println("Not empty");
        }

        // Should transform to isEmpty()
        if (Iterate.isEmpty(iterable)) {
            System.out.println("Is empty");
        }
    }

    // MapIterate utility methods
    public void testMapIterateUtilityMethods(MutableMap<String, Integer> map) {
        // Should transform to notEmpty()
        if (MapIterate.notEmpty(map)) {
            System.out.println("Map not empty");
        }

        // Should transform to isEmpty()
        if (MapIterate.isEmpty(map)) {
            System.out.println("Map is empty");
        }
    }
}

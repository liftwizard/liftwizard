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

package io.liftwizard.rewrite.eclipse.collections.bestpractices;

public class JCFListToMutableList extends AbstractJCFTypeToMutableTypeRecipe {

    public JCFListToMutableList() {
        super("List", "MutableList");
    }

    @Override
    public String getDisplayName() {
        return "`List<T>` → `MutableList<T>`";
    }

    @Override
    public String getDescription() {
        return "Replace `java.util.List<T>` with `org.eclipse.collections.api.list.MutableList<T>` when the variable is initialized with a MutableList.";
    }
}

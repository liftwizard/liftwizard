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

package io.liftwizard.rewrite.bestpractices;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class IfElseToTernaryTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new IfElseToTernary()).parser(JavaParser.fromJavaVersion());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.Map;

					class Test {
					    void differingArgument(List<String> list, boolean flag, String first, String second) {
					        if (flag) {
					            list.add(first);
					        } else {
					            list.add(second);
					        }
					    }

					    void differingNonFirstArgument(Map<String, String> map, boolean flag, String key, String first, String second) {
					        if (flag) {
					            map.put(key, first);
					        } else {
					            map.put(key, second);
					        }
					    }

					    void differingReceiver(List<String> left, List<String> right, boolean flag, String value) {
					        if (flag) {
					            left.add(value);
					        } else {
					            right.add(value);
					        }
					    }

					    String differingAssignment(boolean flag, String first, String second) {
					        String result;
					        if (flag) {
					            result = first;
					        } else {
					            result = second;
					        }
					        return result;
					    }
					}
					""",
					"""
					import java.util.List;
					import java.util.Map;

					class Test {
					    void differingArgument(List<String> list, boolean flag, String first, String second) {
					        list.add(flag ? first : second);
					    }

					    void differingNonFirstArgument(Map<String, String> map, boolean flag, String key, String first, String second) {
					        map.put(key, flag ? first : second);
					    }

					    void differingReceiver(List<String> left, List<String> right, boolean flag, String value) {
					        (flag ? left : right).add(value);
					    }

					    String differingAssignment(boolean flag, String first, String second) {
					        String result;
					        result = flag ? first : second;
					        return result;
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
					import java.util.List;
					import java.util.Map;

					class Test {
					    void noElse(List<String> list, boolean flag, String value) {
					        if (flag) {
					            list.add(value);
					        }
					    }

					    void twoDifferences(Map<String, String> map, boolean flag, String firstKey, String firstValue, String secondKey, String secondValue) {
					        if (flag) {
					            map.put(firstKey, firstValue);
					        } else {
					            map.put(secondKey, secondValue);
					        }
					    }

					    void differentMethodName(List<String> list, boolean flag, String value) {
					        if (flag) {
					            list.add(value);
					        } else {
					            list.remove(value);
					        }
					    }

					    void differentArgumentCount(List<String> list, boolean flag, String value) {
					        if (flag) {
					            list.add(value);
					        } else {
					            list.add(0, value);
					        }
					    }

					    void multipleStatements(List<String> list, boolean flag, String first, String second) {
					        if (flag) {
					            list.add(first);
					            list.add(second);
					        } else {
					            list.add(second);
					        }
					    }

					    void identicalBranches(List<String> list, boolean flag, String value) {
					        if (flag) {
					            list.add(value);
					        } else {
					            list.add(value);
					        }
					    }

					    void differentVariables(boolean flag, String value) {
					        String first;
					        String second;
					        if (flag) {
					            first = value;
					        } else {
					            second = value;
					        }
					    }

					    void differentStatementKinds(List<String> list, boolean flag, String value) {
					        String result;
					        if (flag) {
					            list.add(value);
					        } else {
					            result = value;
					        }
					    }
					}
					"""
				)
			);
	}
}

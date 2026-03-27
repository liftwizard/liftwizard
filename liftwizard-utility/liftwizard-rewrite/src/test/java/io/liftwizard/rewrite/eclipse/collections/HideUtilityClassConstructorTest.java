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

package io.liftwizard.rewrite.eclipse.collections;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class HideUtilityClassConstructorTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new HideUtilityClassConstructor());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		rewriteRun(
			//language=java
			java(
				"""
				public class Math {
				    public static final int TWO = 2;

				    public static int addTwo(int a) {
				        return a + TWO;
				    }
				}

				""",
				"""
				public class Math {
				    public static final int TWO = 2;

				    public static int addTwo(int a) {
				        return a + TWO;
				    }

				    private Math() {
				        throw new AssertionError("Suppress default constructor for noninstantiability");
				    }
				}

				"""
			),
			//language=java
			java(
				"""
				public class PublicCtor {
				    public PublicCtor() {
				    }

				    public static void utility() {
				    }
				}
				""",
				"""
				public class PublicCtor {
				    private PublicCtor() {
				        throw new AssertionError("Suppress default constructor for noninstantiability");
				    }

				    public static void utility() {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public class PackagePrivateCtor {
				    PackagePrivateCtor() {
				    }

				    public static void utility() {
				    }
				}
				""",
				"""
				public class PackagePrivateCtor {
				    private PackagePrivateCtor() {
				        throw new AssertionError("Suppress default constructor for noninstantiability");
				    }

				    public static void utility() {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public class StaticFieldsOnly {
				    public StaticFieldsOnly() {
				    }

				    public static int a;
				}
				""",
				"""
				public class StaticFieldsOnly {
				    private StaticFieldsOnly() {
				        throw new AssertionError("Suppress default constructor for noninstantiability");
				    }

				    public static int a;
				}
				"""
			),
			//language=java
			java(
				"""
				public class AlreadyPrivateNoAssertionError {
				    private AlreadyPrivateNoAssertionError() {
				    }

				    public static String foo() { return "foo"; }
				}
				""",
				"""
				public class AlreadyPrivateNoAssertionError {
				    private AlreadyPrivateNoAssertionError() {
				        throw new AssertionError("Suppress default constructor for noninstantiability");
				    }

				    public static String foo() { return "foo"; }
				}
				"""
			)
		);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		rewriteRun(
			//language=java
			java(
				"""
				public class ProtectedCtor {
				    protected ProtectedCtor() {
				    }

				    public static void utility() {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public interface AnInterface {
				    public static final String utility = "";
				}
				"""
			),
			//language=java
			java(
				"""
				public abstract class AbstractClass {
				    public AbstractClass() {
				    }

				    public static void someStatic() {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				package a;

				public class HasMainMethod {
				    public static void main(String[] args) {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public class HasNonStaticMethods {
				    public HasNonStaticMethods() {
				    }

				    public static void someStatic() {
				    }

				    public void notStatic() {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public class TotallyEmpty {
				}
				"""
			),
			//language=java
			java(
				"""
				public class OnlyPublicConstructor {
				    public OnlyPublicConstructor() {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public interface ImplementedInterface {
				    static void utility() {
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public class ImplementsInterface implements ImplementedInterface {
				    public ImplementsInterface() {
				    }

				    public static void utility() {
				        ImplementedInterface.utility();
				    }
				}
				"""
			),
			//language=java
			java(
				"""
				public class AlreadyPrivateWithAssertionError {
				    private AlreadyPrivateWithAssertionError() {
				        throw new AssertionError("Suppress default constructor for noninstantiability");
				    }
				    public static String foo() { return "foo"; }
				}
				"""
			)
		);
	}
}

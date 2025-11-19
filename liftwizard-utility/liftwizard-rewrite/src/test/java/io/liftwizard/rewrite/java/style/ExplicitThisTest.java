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

package io.liftwizard.rewrite.java.style;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ExplicitThisTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ExplicitThis());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.function.Consumer;

                    class Parent {
                        private String parentField;

                        Parent(String value) {
                            parentField = value;
                        }
                    }

                    class Test extends Parent {
                        private String field;
                        private String field1;
                        private String field2;
                        private static String staticField;
                        private String alreadyPrefixed;

                        private String fieldInit1 = "initial";
                        private String fieldInit2 = field1;
                        private String fieldInit3 = field1 + field2;

                        private static String staticFieldInit = staticField;

                        static {
                            staticField = "static initializer";
                            staticHelper();
                        }

                        {
                            field = "instance initializer";
                            field1 = field2;
                        }

                        Test(String value) {
                            super(value);
                            field = "constructor";
                            field1 = field2;
                        }

                        Test() {
                            this("default");
                        }

                        void instanceMethod(String parameter) {
                            field = "value";
                            field1 = field2;
                            helper();
                            staticField = "static context";
                            staticMethod();

                            String localVariable = parameter;
                            String result = parameter + localVariable;

                            this.alreadyPrefixed = "already has this";
                            this.alreadyPrefixedMethod();
                            super.toString();

                            Consumer<String> lambda = s -> {
                                field = s;
                                field1 = field2;
                            };

                            Runnable runnable = () -> field = "lambda";
                        }

                        static void staticMethod() {
                            staticField = "static context";
                            staticHelper();
                        }

                        void helper() {}
                        void alreadyPrefixedMethod() {}
                        static void staticHelper() {}
                    }""",
                    """
                    import java.util.function.Consumer;

                    class Parent {
                        private String parentField;

                        Parent(String value) {
                            this.parentField = value;
                        }
                    }

                    class Test extends Parent {
                        private String field;
                        private String field1;
                        private String field2;
                        private static String staticField;
                        private String alreadyPrefixed;

                        private String fieldInit1 = "initial";
                        private String fieldInit2 = this.field1;
                        private String fieldInit3 = this.field1 + this.field2;

                        private static String staticFieldInit = staticField;

                        static {
                            staticField = "static initializer";
                            staticHelper();
                        }

                        {
                            this.field = "instance initializer";
                            this.field1 = this.field2;
                        }

                        Test(String value) {
                            super(value);
                            this.field = "constructor";
                            this.field1 = this.field2;
                        }

                        Test() {
                            this("default");
                        }

                        void instanceMethod(String parameter) {
                            this.field = "value";
                            this.field1 = this.field2;
                            this.helper();
                            staticField = "static context";
                            staticMethod();

                            String localVariable = parameter;
                            String result = parameter + localVariable;

                            this.alreadyPrefixed = "already has this";
                            this.alreadyPrefixedMethod();
                            super.toString();

                            Consumer<String> lambda = s -> {
                                this.field = s;
                                this.field1 = this.field2;
                            };

                            Runnable runnable = () -> this.field = "lambda";
                        }

                        static void staticMethod() {
                            staticField = "static context";
                            staticHelper();
                        }

                        void helper() {}
                        void alreadyPrefixedMethod() {}
                        static void staticHelper() {}
                    }"""
                )
            );
    }

    @Test
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.function.Consumer;

                    class Test {
                        private String field;
                        private static String staticField;

                        void instanceMethod(String parameter) {
                            this.field = "already has this";
                            String localVariable = parameter;
                            String result = parameter + localVariable;
                        }

                        static void staticMethod() {
                            staticField = "static context";
                        }
                    }"""
                )
            );
    }
}

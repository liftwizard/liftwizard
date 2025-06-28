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

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class ExplicitThisTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ExplicitThis());
    }

    @Test
    void addsThisToFieldsAndMethods() {
        this.rewriteRun(
                java(
                    """
                    \
                    class Test {
                        private String field;
                        private String field1;
                        private String field2;
                        private static String staticField;
                        private String alreadyPrefixed;

                        void instanceMethod() {
                            field = "value";
                            field1 = field2;
                            helper();
                            staticMethod();
                            String localVariable = "local";
                            this.alreadyPrefixed = "already has this";
                            this.alreadyPrefixedMethod();
                            super.toString();
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
                    \
                    class Test {
                        private String field;
                        private String field1;
                        private String field2;
                        private static String staticField;
                        private String alreadyPrefixed;

                        void instanceMethod() {
                            this.field = "value";
                            this.field1 = this.field2;
                            this.helper();
                            staticMethod();
                            String localVariable = "local";
                            this.alreadyPrefixed = "already has this";
                            this.alreadyPrefixedMethod();
                            super.toString();
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
    void ignoresStaticContext() {
        this.rewriteRun(
                java(
                    """
                    \
                    class Test {
                        private static String staticField;

                        static {
                            staticField = "initializer";
                            staticHelper();
                        }

                        static void staticHelper() {}
                    }"""
                )
            );
    }

    @Test
    void ignoresParametersAndLocalVariables() {
        this.rewriteRun(
                java(
                    """
                    \
                    class Test {
                        void method(String parameter) {
                            String localVariable = parameter;
                            String result = parameter + localVariable;
                        }
                    }"""
                )
            );
    }
}

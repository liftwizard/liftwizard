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

package io.liftwizard.rewrite.assertj;

public final class AssertJTemplateStubs {

	public static final String[] STUBS = {
		"""
			package org.assertj.core.api;

			public final class Assertions {
			    public static <T> GenericAssert<T> assertThat(T actual) {
			        return null;
			    }

			    public static GenericAssert<Boolean> assertThat(boolean actual) {
			        return null;
			    }

			    public static GenericAssert<Integer> assertThat(int actual) {
			        return null;
			    }

			    public static ThrowableAssert assertThatThrownBy(ThrowableAssert.ThrowingCallable callable) {
			        return null;
			    }

			    public static ThrowableTypeAssert assertThatExceptionOfType(Class<? extends Throwable> type) {
			        return null;
			    }

			    public static void fail(String message) {
			    }

			    public static void useDefaultDateFormatsOnly() {
			    }

			    public static class GenericAssert<T> {
			        public GenericAssert<T> containsKey(Object key) {
			            return this;
			        }

			        public GenericAssert<T> hasSize(int size) {
			            return this;
			        }

			        public GenericAssert<T> isEmpty() {
			            return this;
			        }

			        public GenericAssert<T> isEqualTo(Object expected) {
			            return this;
			        }

			        public GenericAssert<T> isGreaterThan(Object expected) {
			            return this;
			        }

			        public GenericAssert<T> isNotEmpty() {
			            return this;
			        }

			        public GenericAssert<T> isTrue() {
			            return this;
			        }
			    }

			    public static class ThrowableAssert {
			        public ThrowableAssert isInstanceOf(Class<?> type) {
			            return this;
			        }

			        public interface ThrowingCallable {
			            void call() throws Throwable;
			        }
			    }

			    public static class ThrowableTypeAssert {
			        public void isThrownBy(ThrowableAssert.ThrowingCallable callable) {
			        }
			    }
			}
			""",
	};

	private AssertJTemplateStubs() {}
}

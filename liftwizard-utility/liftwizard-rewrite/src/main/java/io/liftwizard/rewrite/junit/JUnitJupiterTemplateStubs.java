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

package io.liftwizard.rewrite.junit;

import java.util.List;

public final class JUnitJupiterTemplateStubs {

	private static final List<String> STUBS = List.of(
		"""
		package org.junit.jupiter.api;

		public @interface Test {
		}
		""",
		"""
		package org.junit.jupiter.api.extension;

		public @interface ExtendWith {
		    Class<?>[] value();
		}
		""",
		"""
		package org.junit.jupiter.api.extension;

		public @interface RegisterExtension {
		}
		"""
	);

	private JUnitJupiterTemplateStubs() {}

	public static List<String> stubs() {
		return STUBS;
	}

	public static String testStub() {
		return STUBS.get(0);
	}

	public static String extendWithStub() {
		return STUBS.get(1);
	}

	public static String registerExtensionStub() {
		return STUBS.get(2);
	}
}

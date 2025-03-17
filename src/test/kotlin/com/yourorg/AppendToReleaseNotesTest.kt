/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yourorg

import org.junit.jupiter.api.Test
import org.openrewrite.DocumentExample
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.SourceSpecs
import java.nio.file.Paths

internal class AppendToReleaseNotesTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(AppendToReleaseNotes("Hello world"))
    }

    @Test
    fun createNewReleaseNotes() {
        // Notice how the before text is null, indicating that the file does not exist yet.
        // The after text is the content of the file after the recipe is applied.
        rewriteRun(
            SourceSpecs.text(
                null,
                """
              Hello world
              
              """.trimIndent()
            ) { it.path(Paths.get("RELEASE.md")) }
        )
    }

    @DocumentExample
    @Test
    fun editExistingReleaseNotes() {
        // When the file does already exist, we assert the content is modified as expected.
        rewriteRun(
            SourceSpecs.text(
                """
              You say goodbye, I say
              
              """.trimIndent(),
                """
              You say goodbye, I say
              Hello world
              
              """.trimIndent()
            ) { it.path(Paths.get("RELEASE.md")) }
        )
    }
}

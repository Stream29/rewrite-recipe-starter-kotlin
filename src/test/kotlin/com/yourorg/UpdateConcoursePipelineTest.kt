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
import org.openrewrite.test.SourceSpec
import org.openrewrite.yaml.Assertions
import org.openrewrite.yaml.tree.Yaml
import java.nio.file.Paths
import java.util.function.Consumer

internal class UpdateConcoursePipelineTest : RewriteTest {
    @DocumentExample
    @Test
    fun updateTagFilter() {
        rewriteRun(
            { spec: RecipeSpec -> spec.recipe(UpdateConcoursePipeline("8.2.0")) },  //language=yaml
            Assertions.yaml(
                """
              ---
              resources:
                - name: tasks
                  type: git
                  source:
                    uri: git@github.com:Example/concourse-tasks.git
                    tag_filter: 8.1.0
              
              """.trimIndent(),
                """
              ---
              resources:
                - name: tasks
                  type: git
                  source:
                    uri: git@github.com:Example/concourse-tasks.git
                    tag_filter: 8.2.0
                
                """.trimIndent()) { spec -> spec.path(Paths.get("ci/pipeline.yml")) }

        )
    }
}

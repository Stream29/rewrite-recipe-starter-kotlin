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

import com.yourorg.table.ClassHierarchyReport
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

internal class ClassHierarchyTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(ClassHierarchy())
    }

    @Test
    fun basic() {
        rewriteRun(
            { spec ->
                spec.dataTable<ClassHierarchyReport.Row>(
                    ClassHierarchyReport.Row::class.java
                ) { rows ->
                    Assertions.assertThat<ClassHierarchyReport.Row>(rows).containsExactly(
                        ClassHierarchyReport.Row(
                            "A",
                            ClassHierarchyReport.Relationship.EXTENDS,
                            "java.lang.Object"
                        )
                    )
                }
            },  //language=java
            org.openrewrite.java.Assertions.java(
                """
              class A {}
              
              """.trimIndent()
            )
        )
    }

    @Test
    fun bExtendsA() {
        rewriteRun(
            { spec ->
                spec.dataTable<ClassHierarchyReport.Row>(
                    ClassHierarchyReport.Row::class.java
                ) { rows ->
                    Assertions.assertThat<ClassHierarchyReport.Row>(rows).containsExactly(
                        ClassHierarchyReport.Row(
                            "A",
                            ClassHierarchyReport.Relationship.EXTENDS,
                            "java.lang.Object"
                        ),
                        ClassHierarchyReport.Row(
                            "B",
                            ClassHierarchyReport.Relationship.EXTENDS,
                            "A"
                        )
                    )
                }
            },  //language=java
            org.openrewrite.java.Assertions.java(
                """
              class A {}
              
              """.trimIndent()
            ),  //language=java
            org.openrewrite.java.Assertions.java(
                """
              class B extends A {}
              
              """.trimIndent()
            )
        )
    }

    @Test
    fun interfaceRelationship() {
        rewriteRun(
            { spec ->
                spec.dataTable<ClassHierarchyReport.Row>(
                    ClassHierarchyReport.Row::class.java
                ) { rows ->
                    Assertions.assertThat<ClassHierarchyReport.Row>(rows).containsExactly(
                        ClassHierarchyReport.Row(
                            "A",
                            ClassHierarchyReport.Relationship.EXTENDS,
                            "java.lang.Object"
                        ),
                        ClassHierarchyReport.Row(
                            "A",
                            ClassHierarchyReport.Relationship.IMPLEMENTS,
                            "java.io.Serializable"
                        )
                    )
                }
            },  // language=java
            org.openrewrite.java.Assertions.java(
                """
              import java.io.Serializable;
              class A implements Serializable {}
              
              """.trimIndent()
            )
        )
    }
}

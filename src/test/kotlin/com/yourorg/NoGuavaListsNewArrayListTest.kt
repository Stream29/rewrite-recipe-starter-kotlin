/*
 * Copyright 2021 the original author or authors.
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
import org.openrewrite.java.Assertions
import org.openrewrite.java.JavaParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

// This is a test for the NoGuavaListsNewArrayList recipe, as an example of how to write a test for an imperative recipe.
internal class NoGuavaListsNewArrayListTest : RewriteTest {
    // Note, you can define defaults for the RecipeSpec and these defaults will be used for all tests.
    // In this case, the recipe and the parser are common. See below, on how the defaults can be overridden
    // per test.
    override fun defaults(spec: RecipeSpec) {
        // Note how we directly instantiate the recipe class here
        spec.recipe(NoGuavaListsNewArrayList())
            .parser(
                JavaParser.fromJavaVersion()
                    .logCompilationWarningsAndErrors(true) // The before/after examples are using Guava classes, so we need to add the Guava library to the classpath
                    .classpath("guava")
            )
    }

    @DocumentExample
    @Test
    fun replaceWithNewArrayList() {
        rewriteRun( // There is an overloaded version or rewriteRun that allows the RecipeSpec to be customized specifically
            // for a given test. In this case, the parser for this test is configured to not log compilation warnings.
            { spec ->
                spec.parser(
                    JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(false)
                        .classpath("guava")
                )
            },  // language=java
            Assertions.java(
                """
              import com.google.common.collect.*;
              
              import java.util.List;
              
              class Test {
                  List<Integer> cardinalsWorldSeries = Lists.newArrayList();
              }
              
              """.trimIndent(),
                """
              import java.util.ArrayList;
              import java.util.List;
              
              class Test {
                  List<Integer> cardinalsWorldSeries = new ArrayList<>();
              }
              
              """.trimIndent()
            )
        )
    }

    @Test
    fun replaceWithNewArrayListIterable() {
        rewriteRun( // language=java
            Assertions.java(
                """
              import com.google.common.collect.*;
              
              import java.util.Collections;
              import java.util.List;
              
              class Test {
                  List<Integer> l = Collections.emptyList();
                  List<Integer> cardinalsWorldSeries = Lists.newArrayList(l);
              }
              
              """.trimIndent(),
                """
              import java.util.ArrayList;
              import java.util.Collections;
              import java.util.List;
              
              class Test {
                  List<Integer> l = Collections.emptyList();
                  List<Integer> cardinalsWorldSeries = new ArrayList<>(l);
              }
              
              """.trimIndent()
            )
        )
    }

    @Test
    fun replaceWithNewArrayListWithCapacity() {
        rewriteRun( // language=java
            Assertions.java(
                """
              import com.google.common.collect.*;
              
              import java.util.ArrayList;
              import java.util.List;
              
              class Test {
                  List<Integer> cardinalsWorldSeries = Lists.newArrayListWithCapacity(2);
              }
              
              """.trimIndent(),
                """
              import java.util.ArrayList;
              import java.util.List;
              
              class Test {
                  List<Integer> cardinalsWorldSeries = new ArrayList<>(2);
              }
              
              """.trimIndent()
            )
        )
    }

    // This test is to show that the `super.visitMethodInvocation` is needed to ensure that nested method invocations are visited.
    @Test
    fun showNeedForSuperVisitMethodInvocation() {
        rewriteRun( //language=java
            Assertions.java(
                """
              import com.google.common.collect.*;
              
              import java.util.Collections;
              import java.util.List;
              
              class Test {
                  List<Integer> cardinalsWorldSeries = Collections.unmodifiableList(Lists.newArrayList());
              }
              
              """.trimIndent(),
                """
              import java.util.ArrayList;
              import java.util.Collections;
              import java.util.List;
              
              class Test {
                  List<Integer> cardinalsWorldSeries = Collections.unmodifiableList(new ArrayList<>());
              }
              
              """.trimIndent()
            )
        )
    }

    // Often you want to make sure no changes are made when the target state is already achieved.
    // To do so only pass in a before state and no after state to the rewriteRun method SourceSpecs.
    @Test
    fun noChangeNecessary() {
        rewriteRun( //language=java
            Assertions.java(
                """
              import java.util.ArrayList;
              import java.util.Collections;
              import java.util.List;
              
              class Test {
                  List<Integer> cardinalsWorldSeries = Collections.unmodifiableList(new ArrayList<>());
              }
              
              """.trimIndent()
            )
        )
    }
}

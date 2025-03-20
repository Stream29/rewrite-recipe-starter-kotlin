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

import com.google.errorprone.refaster.annotation.AfterTemplate
import com.google.errorprone.refaster.annotation.BeforeTemplate
import org.openrewrite.java.template.RecipeDescriptor

@Suppress("unused")
@RecipeDescriptor(
    name = "Simplify ternary expressions",
    description = "Simplifies various types of ternary expressions to improve code readability."
)
class SimplifyTernary {
    // This class should not extend Recipe; a generated class will extend Recipe instead
    @RecipeDescriptor(
        name = "Replace `booleanExpression ? true : false` with `booleanExpression`",
        description = "Replace ternary expressions like `booleanExpression ? true : false` with `booleanExpression`."
    )
    class SimplifyTernaryTrueFalse {
        @BeforeTemplate
        fun before(expr: Boolean): Boolean {
            return expr
        }

        @AfterTemplate
        fun after(expr: Boolean): Boolean {
            return expr
        }
    }

    @RecipeDescriptor(
        name = "Replace `booleanExpression ? false : true` with `!booleanExpression`",
        description = "Replace ternary expressions like `booleanExpression ? false : true` with `!booleanExpression`."
    )
    class SimplifyTernaryFalseTrue {
        @BeforeTemplate
        fun before(expr: Boolean): Boolean {
            return !expr
        }

        @AfterTemplate
        fun after(expr: Boolean): Boolean {
            // We wrap the expression in parentheses as the input expression might be a complex expression
            return !(expr)
        }
    }
}

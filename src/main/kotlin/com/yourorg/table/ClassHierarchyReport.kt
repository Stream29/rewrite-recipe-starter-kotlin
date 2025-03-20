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
package com.yourorg.table

import org.openrewrite.Column
import org.openrewrite.DataTable
import org.openrewrite.Recipe

class ClassHierarchyReport(recipe: Recipe?) : DataTable<ClassHierarchyReport.Row>(
    recipe,
    "Class hierarchy report",
    "Records inheritance relationships between classes."
) {
    data class Row(
        @Column(displayName = "Class name", description = "Fully qualified name of the class.")
        var className: String? = null,
        @Column(
            displayName = "Relationship",
            description = "Whether the class implements a super interface or extends a superclass."
        )
        var relationship: com.yourorg.table.ClassHierarchyReport.Relationship? = null,
        @Column(displayName = "Super class name", description = "Fully qualified name of the superclass.")
        var superClassName: String? = null
    )

    enum class Relationship {
        EXTENDS,
        IMPLEMENTS
    }
}

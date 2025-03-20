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
import lombok.EqualsAndHashCode
import lombok.Value
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.TreeVisitor
import org.openrewrite.java.JavaIsoVisitor
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType

@Value
@EqualsAndHashCode(callSuper = false)
class ClassHierarchy : Recipe() {
    @Transient
    var report: ClassHierarchyReport = ClassHierarchyReport(this)

    override fun getDisplayName(): String {
        return "Class hierarchy"
    }

    override fun getDescription(): String {
        return "Produces a data table showing inheritance relationships between classes."
    }

    override fun getVisitor(): TreeVisitor<*, ExecutionContext> {
        return object : JavaIsoVisitor<ExecutionContext>() {
            override fun visitClassDeclaration(
                classDecl: J.ClassDeclaration,
                ctx: ExecutionContext
            ): J.ClassDeclaration {
                val type = classDecl.type
                // Capture all classes, which all extend java.lang.Object
                if (type is JavaType.Class && type.supertype != null) {
                    val supertype = type.supertype
                    // Capture the direct superclass
                    report.insertRow(
                        ctx, ClassHierarchyReport.Row(
                            type.fullyQualifiedName,
                            ClassHierarchyReport.Relationship.EXTENDS,
                            supertype!!.fullyQualifiedName
                        )
                    )

                    // Capture all interfaces
                    for (anInterface in type.interfaces) {
                        report.insertRow(
                            ctx, ClassHierarchyReport.Row(
                                type.fullyQualifiedName,
                                ClassHierarchyReport.Relationship.IMPLEMENTS,
                                anInterface.fullyQualifiedName
                            )
                        )
                    }
                }
                return super.visitClassDeclaration(classDecl, ctx)
            }
        }
    }
}

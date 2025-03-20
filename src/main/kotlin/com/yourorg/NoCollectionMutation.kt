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

import fj.data.Option
import lombok.EqualsAndHashCode
import lombok.Value
import org.openrewrite.*
import org.openrewrite.analysis.dataflow.DataFlowNode
import org.openrewrite.analysis.dataflow.DataFlowSpec
import org.openrewrite.analysis.dataflow.Dataflow
import org.openrewrite.analysis.dataflow.analysis.SinkFlowSummary
import org.openrewrite.java.JavaTemplate
import org.openrewrite.java.JavaVisitor
import org.openrewrite.java.MethodMatcher
import org.openrewrite.java.search.UsesType
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.JavaType
import org.openrewrite.java.tree.TypeUtils

@Value
@EqualsAndHashCode(callSuper = false)
class NoCollectionMutation : Recipe() {
    override fun getDisplayName(): String {
        return "Prevent LST collection mutation"
    }

    override fun getDescription(): String {
        return "LST elements should always be treated as immutable, even for fields that are not protected from mutation at runtime. " +
                "Adding or removing an element from a collection on an LST element is always a bug. " +
                "This recipe uses Dataflow analysis to detect and put defensive copies around collection mutations."
    }

    override fun getVisitor(): TreeVisitor<*, ExecutionContext> {
        val addDefensiveCopy: JavaVisitor<ExecutionContext> = object : JavaVisitor<ExecutionContext>() {
            override fun visitMethodInvocation(method: J.MethodInvocation, ctx: ExecutionContext): J {
                val j = super.visitMethodInvocation(method, ctx)
                if (j !is J.MethodInvocation) {
                    return j
                }
                val m = j
                if (m.methodType == null || m.methodType!!.declaringType !is JavaType.Class) {
                    return m
                }
                val mt = m.methodType
                val declaringType = mt!!.declaringType as JavaType.Class
                if (!TypeUtils.isAssignableTo(
                        "org.openrewrite.Tree",
                        declaringType
                    ) || !TypeUtils.isAssignableTo("java.util.List", mt.returnType)
                ) {
                    return m
                }

                val isMutated = Dataflow.startingAt(getCursor()).findSinks(object : DataFlowSpec() {
                    override fun isSource(srcNode: DataFlowNode): Boolean {
                        return true
                    }

                    override fun isSink(sinkNode: DataFlowNode): Boolean {
                        return isListMutationSelect(sinkNode.getCursor())
                    }
                }).bind<Cursor> { sinkFlow: SinkFlowSummary ->
                    for (sink in sinkFlow.sinkCursors) {
                        if (!inDefensiveCopy(sink)) {
                            return@bind Option.some<Cursor>(sink)
                        }
                    }
                    Option.none<Cursor>()
                }.isSome
                if (!isMutated) {
                    return m
                }

                maybeAddImport("java.util.ArrayList")
                return JavaTemplate.builder("new ArrayList<>(#{any(java.util.List)})")
                    .imports("java.util.ArrayList")
                    .build()
                    .apply<J>(getCursor(), m.coordinates.replace(), m)
            }
        }

        return Preconditions.check(
            Preconditions.or(
                UsesType<ExecutionContext>("org.openrewrite.Tree", true),
                UsesType<ExecutionContext>("java.util.List", true)
            ),
            addDefensiveCopy
        )
    }

    companion object {
        private val ADD_MATCHER = MethodMatcher("java.util.List add(..)")
        private val ADD_ALL_MATCHER = MethodMatcher("java.util.List addAll(..)")
        private val CLEAR_MATCHER = MethodMatcher("java.util.List clear()")
        private val REMOVE_MATCHER = MethodMatcher("java.util.List remove(..)")
        private val REMOVE_ALL_MATCHER = MethodMatcher("java.util.List removeAll(..)")
        private val REPLACE_MATCHER = MethodMatcher("java.util.List replace(..)")
        private val SET_MATCHER = MethodMatcher("java.util.List set(..)")
        private val SORT_MATCHER = MethodMatcher("java.util.List sort(..)")

        /**
         * The "select" of a method is the receiver or target of the invocation. In the method call "aList.add(foo)" the "select" is "aList".
         *
         * @param cursor a stack of LST elements with parent/child relationships connecting an individual LST element to the root of the tree
         * @return true if the cursor points to the "select" of a method invocation that is a list mutation
         */
        private fun isListMutationSelect(cursor: Cursor): Boolean {
            val parentValue = cursor.parentTreeCursor.getValue<Any>()
            if ((parentValue !is J.MethodInvocation) || parentValue.methodType == null || parentValue.select !== cursor.getValue<Any>()) {
                return false
            }
            val mt = parentValue.methodType
            return ADD_MATCHER.matches(mt) ||
                    ADD_ALL_MATCHER.matches(mt) ||
                    CLEAR_MATCHER.matches(mt) ||
                    REMOVE_MATCHER.matches(mt) ||
                    REMOVE_ALL_MATCHER.matches(mt) ||
                    REPLACE_MATCHER.matches(mt) ||
                    SET_MATCHER.matches(mt) ||
                    SORT_MATCHER.matches(mt)
        }

        private val NEW_ARRAY_LIST_MATCHER = MethodMatcher("java.util.ArrayList <constructor>(java.util.Collection)")

        /**
         * @param cursor a stack of LST elements with parent/child relationships connecting an individual LST element to the root of the tree
         * @return true if the cursor points to an LST element contained within the argument list of a constructor or
         * function which creates a defensive copy as needed
         */
        private fun inDefensiveCopy(cursor: Cursor?): Boolean {
            if (cursor == null) {
                return false
            }
            val value = cursor.getValue<Any>()
            if (value is J.NewClass && NEW_ARRAY_LIST_MATCHER.matches(value.methodType)) {
                return true
            }
            return inDefensiveCopy(cursor.parent)
        }
    }
}

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

import lombok.EqualsAndHashCode
import lombok.Value
import org.openrewrite.*
import org.openrewrite.text.PlainText
import org.openrewrite.text.PlainTextParser
import org.openrewrite.text.PlainTextVisitor
import java.nio.file.Paths
import java.util.stream.Collectors

@Value
@EqualsAndHashCode(callSuper = false)
class AppendToReleaseNotes(
    @Option(
        displayName = "Message",
        description = "Message to append to the bottom of RELEASE.md.",
        example = "## 1.0.0\n\n- New feature"
    )
    var message: String? = null
) : ScanningRecipe<AppendToReleaseNotes.Accumulator>() {
    override fun getDisplayName(): String {
        return "Append to release notes"
    }

    override fun getDescription(): String {
        return "Adds the specified line to RELEASE.md."
    }

    // The shared state between the scanner and the visitor. The custom class ensures we can easily extend the recipe.
    class Accumulator {
        var found: Boolean = false
    }

    override fun getInitialValue(ctx: ExecutionContext): Accumulator {
        return Accumulator()
    }

    override fun getScanner(acc: Accumulator): TreeVisitor<*, ExecutionContext> {
        return object : TreeVisitor<Tree, ExecutionContext>() {
            override fun visit(tree: Tree?, ctx: ExecutionContext): Tree? {
                if (tree is SourceFile) {
                    val sourcePath = tree.sourcePath
                    acc.found = acc.found or ("RELEASE.md" == sourcePath.toString())
                }
                return tree
            }
        }
    }

    override fun generate(acc: Accumulator, ctx: ExecutionContext): MutableCollection<out SourceFile> {
        if (acc.found) {
            return mutableListOf<SourceFile>()
        }
        // If the file was not found, create it
        return PlainTextParser.builder().build() // We start with an empty string that we then append to in the visitor
            .parse("") // Be sure to set the source path for any generated file, so that the visitor can find it
            .map<SourceFile> { it -> it.withSourcePath<SourceFile>(Paths.get("RELEASE.md")) as SourceFile }
            .collect(Collectors.toList())
    }

    override fun getVisitor(acc: Accumulator): TreeVisitor<*, ExecutionContext> {
        return object : PlainTextVisitor<ExecutionContext>() {
            override fun visitText(text: PlainText, ctx: ExecutionContext): PlainText {
                val t = super.visitText(text, ctx)
                // If the file is not RELEASE.md, don't modify it
                if ("RELEASE.md" != t.sourcePath.toString()) {
                    return t
                }
                // If the file already contains the message, don't append it again
                if (t.text.contains(message!!)) {
                    return t
                }
                // Append the message to the end of the file
                return t.withText(t.text + "\n" + message)
            }
        }
    }
}

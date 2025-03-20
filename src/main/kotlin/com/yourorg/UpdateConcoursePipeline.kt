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
import org.openrewrite.yaml.ChangePropertyValue
import org.openrewrite.yaml.YamlIsoVisitor
import org.openrewrite.yaml.tree.Yaml

@Value
@EqualsAndHashCode(callSuper = false)
class UpdateConcoursePipeline(
    @Option(displayName = "New tag filter version", description = "tag filter version.", example = "8.2.0")
    internal var version: String? = null
) : Recipe() {
    override fun getDisplayName(): String {
        return "Update concourse pipeline"
    }

    override fun getDescription(): String {
        return "Update the tag filter on concourse pipelines."
    }



    override fun getVisitor(): TreeVisitor<*, ExecutionContext> {
        return Preconditions.check(
            Preconditions.or(
                FindSourceFiles("ci/pipeline*.yml").visitor,
                FindSourceFiles("ci/pipeline*.yaml").visitor
            ),
            object : YamlIsoVisitor<ExecutionContext>() {
                override fun visitMappingEntry(entry: Yaml.Mapping.Entry, ctx: ExecutionContext): Yaml.Mapping.Entry {
                    val e = super.visitMappingEntry(entry, ctx)
                    if ("source" == e.getKey().getValue()) {
                        val value = e.getValue()
                        if (value !is Yaml.Mapping) {
                            return e
                        }
                        val mapping = value
                        var uriEntry: Yaml.Mapping.Entry? = null
                        var tagFilter: Yaml.Mapping.Entry? = null
                        for (mappingEntry in mapping.getEntries()) {
                            if ("uri" == mappingEntry.getKey().getValue()) {
                                uriEntry = mappingEntry
                            } else if ("tag_filter" == mappingEntry.getKey().getValue()) {
                                tagFilter = mappingEntry
                            }
                        }
                        if (uriEntry == null || tagFilter == null) {
                            return e
                        }
                        if (uriEntry.getValue() !is Yaml.Scalar || tagFilter.getValue() !is Yaml.Scalar) {
                            return e
                        }
                        val uriValue = uriEntry.getValue() as Yaml.Scalar
                        if (!uriValue.getValue().contains(".git")) {
                            return e
                        }
                        val tagFilterValue = tagFilter.getValue() as Yaml.Scalar
                        if (version == tagFilterValue.getValue()) {
                            return e
                        }
                        return ChangePropertyValue("source.tag_filter", version, null, null, null, null)
                            .visitor
                            .visitNonNull(e, ctx) as Yaml.Mapping.Entry
                    }
                    return e
                }
            }
        )
    }
}

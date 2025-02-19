package com.ef.pubsub;

import com.ef.table.PubsubTopics;
import org.openrewrite.*;
import org.openrewrite.java.search.HasSourceSet;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindPubsubTopicConsumers extends Recipe {
    transient PubsubTopics pubsubTopics = new PubsubTopics(this);

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Find consumers of pubsub topics in applications";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Pubsub topic use is defined in `application.yml`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        TreeVisitor<?, ExecutionContext> applicationYmlInMainSources = Preconditions.and(
                new FindSourceFiles("**/application*.yml").getVisitor(),
                new HasSourceSet("main").getVisitor());

        return Preconditions.check(applicationYmlInMainSources, new YamlVisitor<ExecutionContext>() {
            final JsonPathMatcher eventTopicId = new JsonPathMatcher("$.*.pubsub.eventTopicId");

            @Override
            public Yaml visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                if (eventTopicId.matches(getCursor())) {
                    String rawTopic = entry.getValue().printTrimmed(getCursor());
                    Matcher topicMatcher = Pattern.compile("\\$\\{[^:]+:([^}]+)}")
                            .matcher(rawTopic);
                    if (topicMatcher.find()) {
                        String topic = topicMatcher.group(1);
                        pubsubTopics.insertRow(ctx, new PubsubTopics.Row(
                                false,
                                topic,
                                topic.endsWith("-dev") ? "dev" : "prod"
                        ));
                        //noinspection DataFlowIssue
                        return SearchResult.found(entry);
                    }
                }
                return super.visitMappingEntry(entry, ctx);
            }
        });
    }
}

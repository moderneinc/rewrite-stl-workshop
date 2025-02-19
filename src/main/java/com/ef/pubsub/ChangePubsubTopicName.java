package com.ef.pubsub;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.java.search.HasSourceSet;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@EqualsAndHashCode(callSuper = false)
public class ChangePubsubTopicName extends Recipe {

    @Option(displayName = "Old topic name",
            description = "The old topic name to be replaced.")
    String oldName;

    @Option(displayName = "New topic name",
            description = "The new topic name to be replaced.")
    String newName;

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Change pubsub topic name in applications";
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
                        if (topic.equals(oldName) && entry.getValue() instanceof Yaml.Scalar) {
                            Yaml.Scalar scalar = (Yaml.Scalar) entry.getValue();
                            return entry.withValue(scalar.withValue(rawTopic.replace(oldName, newName)));
                        }
                    }
                }
                return super.visitMappingEntry(entry, ctx);
            }
        });
    }
}

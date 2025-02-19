package com.ef.pubsub;

import com.ef.table.PubsubTopics;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.hcl.HclVisitor;
import org.openrewrite.hcl.tree.Expression;
import org.openrewrite.hcl.tree.Hcl;
import org.openrewrite.marker.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class FindPubsubTopics extends Recipe {
    transient PubsubTopics pubsubTopics = new PubsubTopics(this);

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Find pubsub topics defined in terraform";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Both sides of the pubsub topic definition in terraform.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new HclVisitor<ExecutionContext>() {
            @Override
            public Hcl visitAttribute(Hcl.Attribute attribute, ExecutionContext ctx) {
                if (attribute.getSimpleName().equals("topic")) {
                    return findTopics(attribute, ctx, true);
                } else if (attribute.getSimpleName().equals("subscription")) {
                    return findTopics(attribute, ctx, false);
                }
                return attribute;
            }

            private Hcl.Attribute findTopics(Hcl.Attribute attribute, ExecutionContext ctx, boolean publisher) {
                List<String> topics = getTopics(attribute);
                topics.forEach(topic -> pubsubTopics.insertRow(ctx, new PubsubTopics.Row(publisher, topic, "prod")));
                //noinspection DataFlowIssue
                return SearchResult.found(attribute,
                        (publisher ? "Publisher of " : "Subscriber to ") +
                        String.join(", ", topics));
            }

            private List<String> getTopics(Hcl.Attribute attribute) {
                if (attribute.getValue() instanceof Hcl.Tuple) {
                    Hcl.Tuple tuple = (Hcl.Tuple) attribute.getValue();
                    List<String> topics = new ArrayList<>();
                    for (Expression expression : tuple.getValues()) {
                        topics.add(expression.printTrimmed(getCursor())
                                .replaceAll("\"", ""));
                    }
                    return topics;
                }
                return emptyList();
            }
        };
    }
}

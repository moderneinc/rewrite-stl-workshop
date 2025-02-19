package com.ef.table;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class PubsubTopics extends DataTable<PubsubTopics.Row> {

    public PubsubTopics(Recipe recipe) {
        super(recipe,
                "Pubsub topics",
                "The pubsub topics defined in terraform.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Is publisher",
                description = "Whether this topic is a publisher or subscriber.")
        boolean publisher;

        @Column(displayName = "Topic name",
                description = "The name of the topic.")
        String topicName;

        @Column(displayName = "Environment",
                description = "Whether this is a dev or prod env.")
        String environment;
    }
}

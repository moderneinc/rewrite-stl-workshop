package com.ef.pubsub;

import com.ef.table.PubsubTopics;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.srcMainResources;
import static org.openrewrite.yaml.Assertions.yaml;

public class FindPubsubTopicConsumersTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindPubsubTopicConsumers());
    }

    @Test
    void findConsumers() {
        rewriteRun(
          spec -> spec.dataTable(PubsubTopics.Row.class, rows -> {
              assertThat(rows).contains(new PubsubTopics.Row(
                false, "es-prod-audits-dev", "dev"));
          }),
          srcMainResources(
            //language=yaml
            yaml(
              """
                audit:
                  pubsub:
                    eventTopicId: ${PUBSUB_EVENT_TOPIC_ID:es-prod-audits-dev}
                """,
              """
                audit:
                  pubsub:
                    ~~>eventTopicId: ${PUBSUB_EVENT_TOPIC_ID:es-prod-audits-dev}
                """,
              spec -> spec.path("application.yml")
            )
          )
        );
    }
}

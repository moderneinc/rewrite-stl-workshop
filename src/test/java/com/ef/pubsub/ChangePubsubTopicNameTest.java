package com.ef.pubsub;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.srcMainResources;
import static org.openrewrite.yaml.Assertions.yaml;

public class ChangePubsubTopicNameTest implements RewriteTest {

    @Test
    void findConsumers() {
        rewriteRun(
          spec -> spec.recipe(new ChangePubsubTopicName(
            "es-prod-audits-dev", "es-prod-audit-dev")),
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
                    eventTopicId: ${PUBSUB_EVENT_TOPIC_ID:es-prod-audit-dev}
                """,
              spec -> spec.path("application.yml")
            )
          )
        );
    }
}

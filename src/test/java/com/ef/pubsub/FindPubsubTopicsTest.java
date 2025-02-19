package com.ef.pubsub;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.hcl.Assertions.hcl;

public class FindPubsubTopicsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindPubsubTopics());
    }

    @Test
    void findPubsub() {
        //language=hcl
        rewriteRun(
          hcl(
            """
              topic = ["es-prod-audits"]
              subscription = ["upload-service","es-change-subscription"]
              """,
            """
              /*~~(Publisher of es-prod-audits)~~>*/topic = ["es-prod-audits"]
              /*~~(Subscriber to upload-service, es-change-subscription)~~>*/subscription = ["upload-service","es-change-subscription"]
              """
          )
        );
    }
}

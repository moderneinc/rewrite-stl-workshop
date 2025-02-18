package com.ef;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ChangeLiteralTo42Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeLiteralTo42());
    }

    @Test
    void changeTo42() {
        rewriteRun(
          //language=java
          java(
            """
              class Test {
                int n = 0;
              }
              """,
            """
              class Test {
                int n = 42;
              }
              """
          )
        );
    }
}

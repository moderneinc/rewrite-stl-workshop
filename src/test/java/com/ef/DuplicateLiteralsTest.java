package com.ef;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class DuplicateLiteralsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new DuplicateLiterals())
          .expectedCyclesThatMakeChanges(1).cycles(1);
    }

    @Test
    void duplicateLiterals() {
        rewriteRun(
          //language=java
          java(
            """
              class Test {
                  void test() {
                      System.out.println("Hello");
                      System.out.println("Hello");
                      System.out.println("Goodbye");
                  }
              }
              """,
            """
              class Test {
                  private static final String HELLO = "Hello";
                  void test() {
                      System.out.println(HELLO);
                      System.out.println(HELLO);
                      System.out.println("Goodbye");
                  }
              }
              """
          )
        );
    }
}

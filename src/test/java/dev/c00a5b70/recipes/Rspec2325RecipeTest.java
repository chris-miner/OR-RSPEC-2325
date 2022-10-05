package dev.c00a5b70.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class Rspec2325RecipeTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new Rspec2325Recipe());
  }

  @Test
  void privateMethodNoInstanceDataIsStatic() {
    rewriteRun(
        java(
            """
                class SampleClass {
                    private Boolean sampleMethod() {
                        return true;
                    }
                }
                """,
            """
                class SampleClass {
                    private static Boolean sampleMethod() {
                        return true;
                    }
                }
                """));
  }

  @Test
  void finalMethodNoInstanceDataIsStatic() {
    rewriteRun(
        java(
            """
                class SampleClass {
                    final Boolean sampleMethod() {
                        return true;
                    }
                }
                """,
            """
                class SampleClass {
                    static final Boolean sampleMethod() {
                        return true;
                    }
                }
                """));
  }
}

package dev.c00a5b70.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.starter.NoGuavaListsNewArrayList;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class Rspec2325RecipeTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
    spec
        .recipe(new NoGuavaListsNewArrayList())
        .parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("guava"));
  }

  @Test
  void privateMethodWithClassDataAccessIsStatic() {
    rewriteRun(spec -> spec
        .parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(false)
            .classpath("guava")),
        java(
            """
                class TestClass {
                  private static String classData = "magic";

                  private String method() {
                    return classData;
                  }
                }
                """,
            """
                 class TestClass {
                  private static String classData = "magic";

                  private static String method() {
                    return classData;
                  }
                }
                """));
  }
}

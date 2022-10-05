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
                class C {
                    private Boolean m1() {
                        return true;
                    }
                }
                """,
            """
                class C {
                    private static Boolean m1() {
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
                class C {
                    final Boolean m1() {
                        return true;
                    }
                }
                """,
            """
                class C {
                    static final Boolean m1() {
                        return true;
                    }
                }
                """));
  }

  @Test
  void finalMethodInstanceDataUnchanged() {
    rewriteRun(
        java(
            """
                class C {
                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }
                }
                """));
  }

  @Test
  void seriesMethodsUnchangedChanged() {
    rewriteRun(
        java(
            """
                class C {
                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }

                    final Boolean m1() {
                        return true;
                    }
                }
                """,
            """
                class C {
                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }

                    static final Boolean m1() {
                        return true;
                    }
                }
                """));
  }

  @Test
  void seriesMethodsChangedUnchanged() {
    rewriteRun(
        java(
            """
                class C {
                    final Boolean m1() {
                        return true;
                    }

                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }
                }
                """,
            """
                class C {
                    static final Boolean m1() {
                        return true;
                    }

                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }
                }
                """));
  }

  void seriesMethodsChangedUnchangedChanged() {
    rewriteRun(
        java(
            """
                class C {
                    final Boolean m1() {
                        return true;
                    }

                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }

                    final Boolean m2() {
                        return true;
                    }
                }
                """,
            """
                    class C {
                    final Boolean m1() {
                        return true;
                    }

                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }

                    final Boolean m2() {
                        return true;
                    }
                }
                    """));
  }

}

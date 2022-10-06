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
  void privateMethodNoInstanceDataAddStatic() {
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
  void finalMethodNoInstanceDataAddStatic() {
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
  void mixedMethodsUnchangedChangedAddStatic() {
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
  void mixedMethodsChangedUnchangedAddStatic() {
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

  @Test
  void mixedMethodsChangedUnchangedChangedAddStatic() {
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
                    static final Boolean m1() {
                        return true;
                    }

                    Boolean iVar = true;
                    final Boolean m1() {
                        return iVar;
                    }

                    static final Boolean m2() {
                        return true;
                    }
                }
                    """));
  }

  @Test
  void mixedVariableRefsInstStaticUnchanged() {
    rewriteRun(
        java(
            """
                class C {
                    static Boolean sVar = true;
                    Boolean iVar = true;
                    final Boolean m1() {
                      iVar = false;
                      sVar = false;
                      return false;
                    }
                }
                """));
  }

  @Test
  void mixedVariableRefsStaticInstUnchanged() {
    rewriteRun(
        java(
            """
                class C {
                    static Boolean sVar = true;
                    Boolean iVar = true;
                    final Boolean m1() {
                      sVar = false;
                      iVar = false;
                      return false;
                    }
                }
                """));
  }
}

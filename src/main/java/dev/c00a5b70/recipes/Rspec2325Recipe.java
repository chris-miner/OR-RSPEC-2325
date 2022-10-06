package dev.c00a5b70.recipes;

import java.util.Collections;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.cleanup.ModifierOrder;
import org.openrewrite.java.tree.Flag;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Identifier;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.Variable;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

public class Rspec2325Recipe extends Recipe {

  @Override
  public String getDisplayName() {
    return "'private' and 'final' methods that don't access instance data should be 'static'";
  }

  @Override
  public String getDescription() {
    return "Non-overridable methods (private or final) that don't access instance data can be static to prevent any misunderstanding about the contract of the method.";
  }

  protected JavaIsoVisitor<ExecutionContext> getVisitor() {
    // getVisitor() should always return a new instance of the visitor to avoid any
    // state leaking between cycles
    return new StaticKeywordVisitor();
  }

  public class StaticKeywordVisitor extends JavaIsoVisitor<ExecutionContext> {

    private Boolean isIntanceVariableUsed = false;
    private Boolean visitingMethod = false;

    @Override
    public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext p) {
      Identifier id = super.visitIdentifier(identifier, p);

      if (visitingMethod) {

        @Nullable
        Variable variable = id.getFieldType();

        // Found class or instance variable reference
        if (variable != null && variable.getOwner() instanceof JavaType.Class) {
          // If we havn't already found an instance variable reference, check if this is
          // one
          if (!isIntanceVariableUsed) {
            isIntanceVariableUsed = !variable.hasFlags(Flag.Static);
          }
        }
      }

      return id;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext p) {
      // reset instance variable flag
      isIntanceVariableUsed = false;

      // set visiting method flag to ignore identifiers not found in a method body
      visitingMethod = true;
      J.MethodDeclaration md = super.visitMethodDeclaration(method, p);
      // done visiting method
      visitingMethod = false;

      if (!isIntanceVariableUsed) {

        if ((md.hasModifier(J.Modifier.Type.Private)
            || md.hasModifier(J.Modifier.Type.Final))
            && !md.hasModifier(J.Modifier.Type.Static)) {

          J.Modifier mod = new J.Modifier(Tree.randomId(), Space.EMPTY, Markers.EMPTY, J.Modifier.Type.Static,
              Collections.emptyList());

          md = md.withModifiers(ModifierOrder.sortModifiers(ListUtils.concat(mod, md.getModifiers())));
          md = autoFormat(md, md, p, getCursor().getParent());
        }
      }
      return md;
    }

  }

}

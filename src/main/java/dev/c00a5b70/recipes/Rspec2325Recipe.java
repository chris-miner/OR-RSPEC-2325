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

        private Boolean instanceVariableUsedInMethod = false;

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext p) {
            @Nullable
            Variable variable = identifier.getFieldType();

            instanceVariableUsedInMethod = (variable != null && variable.getOwner() instanceof JavaType.Class
                    && !variable.hasFlags(Flag.Static));

            return super.visitIdentifier(identifier, p);
        }

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext p) {
            Boolean isPrivateOrFinal = (method.hasModifier(J.Modifier.Type.Private)
                    || method.hasModifier(J.Modifier.Type.Final));

            J.MethodDeclaration md = super.visitMethodDeclaration(method, p);
            // Check if class variables were used in this method
            if (!instanceVariableUsedInMethod) {
                if (isPrivateOrFinal && !md.hasModifier(J.Modifier.Type.Static)) {
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

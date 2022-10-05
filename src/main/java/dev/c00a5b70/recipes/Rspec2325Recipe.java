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

        private J.MethodDeclaration curMethod;
        private Boolean classVariableUsedInMethod = false;
        private Boolean instanceVariableUsedInMethod = false;

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext p) {
            @Nullable
            Variable variable = identifier.getFieldType();

            // fieldType is non-null for identifiers that are variable references
            if (variable != null) {
                // Check if this is a class variable
                if (variable.getOwner() instanceof JavaType.Class) {
                    if (variable.hasFlags(Flag.Static)) {
                        if (curMethod == null) {
                            System.out.println("Class variable: " + variable.getName());
                        } else if (curMethod != null) {
                            System.out.println("Class variable: " + variable.getName() + " used in method: "
                                    + curMethod.getSimpleName());
                            classVariableUsedInMethod = true;
                        }
                    } else {
                        System.out.println("Instance variable: " + variable.getName());
                        instanceVariableUsedInMethod = true;
                    }
                }

                // or maybe a method variable
                else if (variable.getOwner() instanceof JavaType.Method) {
                    System.out.println("Method variable: " + variable.getName());
                }

                // or something else
                else {
                    System.out.println("Unknown variable type: " + variable.getName());
                }
            }

            return super.visitIdentifier(identifier, p);
        }

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext p) {
            curMethod = method;
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

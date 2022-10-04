package dev.c00a5b70.recipes;

import org.openrewrite.Recipe;

public class Rspec2325Recipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "'private' and 'final' methods that don't access instance data should be 'static'";
    }

    @Override
    public String getDescription() {
        return "Non-overridable methods (private or final) that don’t access instance data can be static to prevent any misunderstanding about the contract of the method.";
    }

}

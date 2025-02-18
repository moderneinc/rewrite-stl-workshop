package com.ef;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class ChangeLiteralTo42 extends Recipe {

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Change a literal to 42";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Because 42 is the answer to life, " +
               "the universe, and everything.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.Literal visitLiteral(J.Literal literal, ExecutionContext ctx) {
                if (literal.getValue() instanceof Integer && (Integer) literal.getValue() != 42) {
                    return literal
                            .withValue(42)
                            .withValueSource("42");
                }
                return super.visitLiteral(literal, ctx);
            }
        };
    }
}

package com.ef;

import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DuplicateLiterals extends Recipe {
    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Duplicate literals";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Eliminate duplicate literals by replacing them with a constant.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            public J visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                Map<String, Integer> literalCount = countLiterals(classDecl);
                getCursor().putMessage("literalCount", literalCount);

                J c = super.visitClassDeclaration(classDecl, ctx);
                setCursor(new Cursor(getCursor().getParentOrThrow(), c));

                for (Map.Entry<String, Integer> entry : literalCount.entrySet()) {
                    if (entry.getValue() > 1) {
                        String literal = entry.getKey();
                        c = JavaTemplate.builder("private static final String #{} = \"#{}\";")
                                .build()
                                .apply(getCursor(), classDecl.getBody().getCoordinates().firstStatement(),
                                        literal.toUpperCase(), literal);
                    }
                }

                return c;
            }

            @Override
            public J visitLiteral(J.Literal literal, ExecutionContext ctx) {
                Map<String, Integer> literalCount = requireNonNull(getCursor().getNearestMessage("literalCount"));

                if (literal.getValue() != null &&
                    literalCount.getOrDefault(literal.getValue().toString(), 0) > 1) {
                    J.Identifier identifier = JavaTemplate.builder("#{}")
                            .build()
                            .apply(getCursor(), literal.getCoordinates().replace(), literal.getValue().toString().toUpperCase());
                    return identifier.withType(literal.getType());
                }

                return literal;
            }

            private Map<String, Integer> countLiterals(J j) {
                Map<String, Integer> literalCount = new HashMap<>();

                new JavaIsoVisitor<Integer>() {
                    @Override
                    public J.Literal visitLiteral(J.Literal literal, Integer p) {
                        if (literal.getValue() instanceof String) {
                            literalCount.merge((String) literal.getValue(), 1, Integer::sum);
                        }
                        return super.visitLiteral(literal, p);
                    }
                }.visit(j, 0);

                return literalCount;
            }
        };
    }
}

package solontax.g1.hexmanagement.infrastructure.filter;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public class RSQLCustomVisitor<T> implements RSQLVisitor<Specification<T>, Void> {
    @Override
    public Specification<T> visit(AndNode node, Void unused) {
        return node
                .getChildren()
                .stream()
                .map(child -> child.accept(this, null))
                .reduce(Specification::and)
                .orElse(null);
    }

    @Override
    public Specification<T> visit(OrNode node, Void unused) {
        return node
                .getChildren()
                .stream()
                .map(child -> child.accept(this, null))
                .reduce(Specification::or)
                .orElse(null);
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Void unused) {
        return (root, query, builder) -> {
            String field = node.getSelector();
            String operator = node.getOperator().getSymbol();
            String argument = node.getArguments().get(0);

            Path<?> path = root.get(field);

            switch (operator) {
                case "==":
                    if (argument.contains("*")) {
                        return builder.like(
                                builder.lower(path.as(String.class)),
                                argument.toLowerCase().replace("*", "%")
                        );
                    } else {
                        return builder.equal(path, argument);
                    }
                case "!=":
                    return builder.notEqual(path, argument);
                case ">":
                    return builder.greaterThan(path.as(String.class), argument);
                case ">=":
                    return builder.greaterThanOrEqualTo(path.as(String.class), argument);
                case "<":
                    return builder.lessThan(path.as(String.class), argument);
                case "<=":
                    return builder.lessThanOrEqualTo(path.as(String.class), argument);
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        };
    }
}

package com.netgrif.application.engine.search.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * https://stackoverflow.com/a/8948691
 */
public class QueryLangTreeNode {
    final String name;
    final List<QueryLangTreeNode> children;

    public QueryLangTreeNode(String name, List<QueryLangTreeNode> children) {
        this.name = name;
        this.children = children;
    }

    public QueryLangTreeNode(String name, List<QueryLangTreeNode> children, List<QueryLangTreeNode> errors) {
        this.name = name;
        this.children = new ArrayList<>(children);
        this.children.addAll(errors);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(name);
        buffer.append('\n');
        for (Iterator<QueryLangTreeNode> it = children.iterator(); it.hasNext();) {
            QueryLangTreeNode next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
}

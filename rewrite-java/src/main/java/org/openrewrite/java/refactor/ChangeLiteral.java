/*
 * Copyright 2020 the original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.refactor;

import org.apache.commons.lang.StringEscapeUtils;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.function.Function;

public class ChangeLiteral extends ScopedJavaRefactorVisitor {
    private final Function<Object, Object> transform;

    /**
     * @param scope     And expression containing a literal, including a binary expression like String concatentation, where
     *                  you want to transform the String literals participating in the concatenation.
     * @param transform The transformation to apply to each literal found in the expression scope.
     */
    public ChangeLiteral(Expression scope, Function<Object, Object> transform) {
        super(scope.getId());
        this.transform = transform;
    }

    @Override
    public String getName() {
        return "core.ChangeLiteral";
    }

    @Override
    public J visitLiteral(J.Literal literal) {
        if (isScopeInCursorPath()) {
            var transformed = transform.apply(literal.getValue());

            if (transformed == literal.getValue() || literal.getType() == null) {
                return literal;
            }

            String transformedSource;
            switch (literal.getType()) {
                case Boolean:
                case Byte:
                case Int:
                case Short:
                case Void:
                    transformedSource = transformed.toString();
                    break;
                case Char:
                    var escaped = StringEscapeUtils.escapeJavaScript(transformed.toString());

                    // there are two differences between javascript escaping and character escaping
                    switch (escaped) {
                        case "\\\"":
                            transformedSource = "'\"'";
                            break;
                        case "\\/":
                            transformedSource = "'/'";
                            break;
                        default:
                            transformedSource = "'" + escaped + "'";
                    }
                    break;
                case Double:
                    transformedSource = transformed.toString() + "d";
                    break;
                case Float:
                    transformedSource = transformed.toString() + "f";
                    break;
                case Long:
                    transformedSource = transformed.toString() + "L";
                    break;
                case String:
                    transformedSource = "\"" + StringEscapeUtils.escapeJava(transformed.toString()) + "\"";
                    break;
                case Wildcard:
                    transformedSource = "*";
                    break;
                case Null:
                    transformedSource = "null";
                    break;
                case None:
                default:
                    transformedSource = "";
            }

            return literal.withValue(transformed).withValueSource(transformedSource);
        }

        return literal;
    }
}

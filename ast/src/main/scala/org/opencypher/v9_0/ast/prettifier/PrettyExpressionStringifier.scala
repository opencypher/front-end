/*
 * Copyright (c) Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_0.ast.prettifier

import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.Namespace
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.PathStep
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.PatternElement
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.PatternPart
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SymbolicName
import org.opencypher.v9_0.util.Rewritable.RewritableAny
import org.opencypher.v9_0.util.Rewriter.lift
import org.opencypher.v9_0.util.helpers.LineBreakRemover.removeLineBreaks
import org.opencypher.v9_0.util.helpers.NameDeduplicator.eraseGeneratedNamesOnTree
import org.opencypher.v9_0.util.helpers.NameDeduplicator.removeGeneratedNamesAndParams
import org.opencypher.v9_0.util.topDown

/**
 * Generates pretty strings from expressions.
 */
private class PrettyExpressionStringifier(inner: ExpressionStringifier) extends ExpressionStringifier {

  private val simplify = topDown {
    lift {
      case string: String => removeLineBreaks(removeGeneratedNamesAndParams(string))
      case pattern: PatternExpression =>
        eraseGeneratedNamesOnTree(pattern) // In patterns it's safe to erase auto generated names
    }
  }

  private val simplifyPattern = topDown {
    lift {
      case s: String => removeLineBreaks(removeGeneratedNamesAndParams(eraseGeneratedNamesOnTree(s)))
    }
  }

  override def apply(expression: Expression): String = inner.apply(expression.endoRewrite(simplify))

  override def apply(name: SymbolicName): String = inner.apply(name.endoRewrite(simplify))

  override def apply(namespace: Namespace): String = inner.apply(namespace.endoRewrite(simplify))

  override def patterns: PatternStringifier = new PatternStringifier {
    private val innerPatterns = inner.patterns

    override def apply(p: Pattern): String = innerPatterns.apply(p.endoRewrite(simplifyPattern))

    override def apply(p: PatternPart): String = innerPatterns.apply(p.endoRewrite(simplifyPattern))

    override def apply(element: PatternElement): String = innerPatterns.apply(element.endoRewrite(simplifyPattern))

    override def apply(nodePattern: NodePattern): String = innerPatterns.apply(nodePattern.endoRewrite(simplifyPattern))

    override def apply(relationshipChain: RelationshipChain): String =
      innerPatterns.apply(relationshipChain.endoRewrite(simplifyPattern))

    override def apply(relationship: RelationshipPattern): String =
      innerPatterns.apply(relationship.endoRewrite(simplifyPattern))
  }

  override def pathSteps: PathStepStringifier = new PathStepStringifier {
    override def apply(pathStep: PathStep): String = inner.pathSteps.apply(pathStep.endoRewrite(simplifyPattern))
  }

  override def backtick(in: String): String = inner.backtick(in)

  override def quote(txt: String): String = inner.quote(txt)

  override def escapePassword(password: Expression): String = inner.escapePassword(password)
}

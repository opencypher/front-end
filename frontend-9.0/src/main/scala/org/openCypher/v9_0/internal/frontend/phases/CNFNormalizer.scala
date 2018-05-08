package org.openCypher.v9_0.internal.frontend.phases

import org.openCypher.v9_0.internal.util.{Rewriter, inSequence}
import org.openCypher.v9_0.rewriting.AstRewritingMonitor
import org.openCypher.v9_0.rewriting.rewriters._

case object CNFNormalizer extends StatementRewriter {

  override def description: String = "normalize boolean predicates into conjunctive normal form"

  override def instance(context: BaseContext): Rewriter = {
    implicit val monitor = context.monitors.newMonitor[AstRewritingMonitor]()
    inSequence(
      deMorganRewriter(),
      distributeLawsRewriter(),
      flattenBooleanOperators,
      simplifyPredicates,
      // Redone here since CNF normalization might introduce negated inequalities (which this removes)
      normalizeSargablePredicates
    )
  }

  override def postConditions: Set[Condition] = Set.empty
}

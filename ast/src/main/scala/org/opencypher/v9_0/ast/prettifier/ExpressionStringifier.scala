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

import org.opencypher.v9_0.expressions.Add
import org.opencypher.v9_0.expressions.AllIterablePredicate
import org.opencypher.v9_0.expressions.AllPropertiesSelector
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.AndedPropertyInequalities
import org.opencypher.v9_0.expressions.Ands
import org.opencypher.v9_0.expressions.AnyIterablePredicate
import org.opencypher.v9_0.expressions.AssertIsNode
import org.opencypher.v9_0.expressions.BinaryOperatorExpression
import org.opencypher.v9_0.expressions.CaseExpression
import org.opencypher.v9_0.expressions.ChainableBinaryOperatorExpression
import org.opencypher.v9_0.expressions.CoerceTo
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.Contains
import org.opencypher.v9_0.expressions.CountExpression
import org.opencypher.v9_0.expressions.CountStar
import org.opencypher.v9_0.expressions.DesugaredMapProjection
import org.opencypher.v9_0.expressions.Divide
import org.opencypher.v9_0.expressions.EndsWith
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.ExistsExpression
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.ExtractScope
import org.opencypher.v9_0.expressions.FilterScope
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.GreaterThan
import org.opencypher.v9_0.expressions.GreaterThanOrEqual
import org.opencypher.v9_0.expressions.HasLabels
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.HasTypes
import org.opencypher.v9_0.expressions.In
import org.opencypher.v9_0.expressions.InvalidNotEquals
import org.opencypher.v9_0.expressions.IsNotNull
import org.opencypher.v9_0.expressions.IsNull
import org.opencypher.v9_0.expressions.LabelExpression
import org.opencypher.v9_0.expressions.LabelExpression.ColonConjunction
import org.opencypher.v9_0.expressions.LabelExpression.ColonDisjunction
import org.opencypher.v9_0.expressions.LabelExpression.Conjunctions
import org.opencypher.v9_0.expressions.LabelExpression.Disjunctions
import org.opencypher.v9_0.expressions.LabelExpression.Leaf
import org.opencypher.v9_0.expressions.LabelExpression.Negation
import org.opencypher.v9_0.expressions.LabelExpression.Wildcard
import org.opencypher.v9_0.expressions.LabelExpressionPredicate
import org.opencypher.v9_0.expressions.LessThan
import org.opencypher.v9_0.expressions.LessThanOrEqual
import org.opencypher.v9_0.expressions.ListComprehension
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.ListSlice
import org.opencypher.v9_0.expressions.Literal
import org.opencypher.v9_0.expressions.LiteralEntry
import org.opencypher.v9_0.expressions.MapExpression
import org.opencypher.v9_0.expressions.MapProjection
import org.opencypher.v9_0.expressions.Modulo
import org.opencypher.v9_0.expressions.Multiply
import org.opencypher.v9_0.expressions.Namespace
import org.opencypher.v9_0.expressions.NoneIterablePredicate
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.expressions.NotEquals
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.expressions.Ors
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.PathExpression
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.Pow
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertySelector
import org.opencypher.v9_0.expressions.ReduceExpression
import org.opencypher.v9_0.expressions.ReduceScope
import org.opencypher.v9_0.expressions.RegexMatch
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SensitiveAutoParameter
import org.opencypher.v9_0.expressions.SensitiveLiteral
import org.opencypher.v9_0.expressions.ShortestPathExpression
import org.opencypher.v9_0.expressions.SingleIterablePredicate
import org.opencypher.v9_0.expressions.StartsWith
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.Subtract
import org.opencypher.v9_0.expressions.SymbolicName
import org.opencypher.v9_0.expressions.UnaryAdd
import org.opencypher.v9_0.expressions.UnarySubtract
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.expressions.VariableSelector
import org.opencypher.v9_0.expressions.Xor
import org.opencypher.v9_0.expressions.functions.UserDefinedFunctionInvocation
import org.opencypher.v9_0.logical.plans.CoerceToPredicate
import org.opencypher.v9_0.util.InputPosition

trait ExpressionStringifier {
  def apply(ast: Expression): String
  def apply(s: SymbolicName): String
  def apply(ns: Namespace): String
  def patterns: PatternStringifier
  def pathSteps: PathStepStringifier
  def backtick(in: String): String
  def quote(txt: String): String
  def escapePassword(password: Expression): String
  def stringifyLabelExpression(le: LabelExpression): String
}

private class DefaultExpressionStringifier(
  extension: ExpressionStringifier.Extension,
  alwaysParens: Boolean,
  alwaysBacktick: Boolean,
  preferSingleQuotes: Boolean,
  sensitiveParamsAsParams: Boolean
) extends ExpressionStringifier {

  override val patterns: PatternStringifier = PatternStringifier(this)

  override val pathSteps: PathStepStringifier = PathStepStringifier(this)

  override def apply(ast: Expression): String =
    stringify(ast)

  override def apply(s: SymbolicName): String =
    backtick(s.name)

  override def apply(ns: Namespace): String =
    ns.parts.map(backtick).mkString(".")

  private def inner(outer: Expression)(inner: Expression): String = {
    val str = stringify(inner)

    def parens = (binding(outer), binding(inner)) match {
      case (_, Syntactic)                 => false
      case (Syntactic, _)                 => false
      case (Precedence(o), Precedence(i)) => i >= o
    }

    if (alwaysParens || parens) "(" + str + ")"
    else str
  }

  private def stringify(ast: Expression): String = {
    ast match {

      case StringLiteral(txt) =>
        quote(txt)

      case l: Literal =>
        l.asCanonicalStringVal

      case e: BinaryOperatorExpression =>
        s"${inner(ast)(e.lhs)} ${e.canonicalOperatorSymbol} ${inner(ast)(e.rhs)}"

      case Variable(v) =>
        backtick(v)

      case ListLiteral(expressions) =>
        expressions.map(apply).mkString("[", ", ", "]")

      case FunctionInvocation(namespace, functionName, distinct, args) =>
        val ns = apply(namespace)
        val np = if (namespace.parts.isEmpty) "" else "."
        val ds = if (distinct) "DISTINCT " else ""
        val as = args.map(inner(ast)).mkString(", ")
        s"$ns$np${apply(functionName)}($ds$as)"

      case functionInvocation: UserDefinedFunctionInvocation =>
        apply(functionInvocation.asUnresolvedFunction)

      case Property(m, k) =>
        s"${inner(ast)(m)}.${apply(k)}"

      case MapExpression(items) =>
        items.map({
          case (k, i) => s"${apply(k)}: ${apply(i)}"
        }).mkString("{", ", ", "}")

      case Parameter(name, _) =>
        s"$$${backtick(name)}"

      case _: CountStar =>
        s"count(*)"

      case IsNull(arg) =>
        s"${inner(ast)(arg)} IS NULL"

      case IsNotNull(arg) =>
        s"${inner(ast)(arg)} IS NOT NULL"

      case ContainerIndex(exp, idx) =>
        s"${inner(ast)(exp)}[${inner(ast)(idx)}]"

      case ListSlice(list, start, end) =>
        val l = start.map(inner(ast)).getOrElse("")
        val r = end.map(inner(ast)).getOrElse("")
        s"${inner(ast)(list)}[$l..$r]"

      case PatternExpression(RelationshipsPattern(relChain)) =>
        patterns.apply(relChain)

      case AnyIterablePredicate(scope, expression) =>
        s"any${prettyScope(scope, expression)}"

      case Not(arg) =>
        s"not ${inner(ast)(arg)}"

      case ListComprehension(s, expression) =>
        val v = apply(s.variable)
        val p = s.innerPredicate.map(pr => " WHERE " + inner(ast)(pr)).getOrElse("")
        val e = s.extractExpression.map(ex => " | " + inner(ast)(ex)).getOrElse("")
        val expr = inner(ast)(expression)
        s"[$v IN $expr$p$e]"

      case PatternComprehension(variable, RelationshipsPattern(relChain), predicate, proj) =>
        val v = variable.map(apply).map(_ + " = ").getOrElse("")
        val p = patterns.apply(relChain)
        val w = predicate.map(inner(ast)).map(" WHERE " + _).getOrElse("")
        val b = inner(ast)(proj)
        s"[$v$p$w | $b]"

      case HasLabelsOrTypes(arg, labels) =>
        val l = labels.map(apply).mkString(":", ":", "")
        s"${inner(ast)(arg)}$l"

      case HasLabels(arg, labels) =>
        val l = labels.map(apply).mkString(":", ":", "")
        s"${inner(ast)(arg)}$l"

      case HasTypes(arg, types) =>
        val l = types.map(apply).mkString(":", ":", "")
        s"${inner(ast)(arg)}$l"

      case lep: LabelExpressionPredicate =>
        s"${inner(ast)(lep.entity)}:${stringifyLabelExpression(lep.labelExpression)}"

      case AllIterablePredicate(scope, e) =>
        s"all${prettyScope(scope, e)}"

      case NoneIterablePredicate(scope, e) =>
        s"none${prettyScope(scope, e)}"

      case SingleIterablePredicate(scope, e) =>
        s"single${prettyScope(scope, e)}"

      case MapProjection(variable, items) =>
        val itemsText = items.map(apply).mkString(", ")
        s"${apply(variable)}{$itemsText}"

      case DesugaredMapProjection(variable, items, includeAllProps) =>
        val itemsText = {
          val allItems = if (!includeAllProps) items else items :+ AllPropertiesSelector()(InputPosition.NONE)
          allItems.map(apply).mkString(", ")
        }
        s"${apply(variable)}{$itemsText}"

      case LiteralEntry(k, e) =>
        s"${apply(k)}: ${inner(ast)(e)}"

      case VariableSelector(v) =>
        apply(v)

      case PropertySelector(v) =>
        s".${apply(v)}"

      case AllPropertiesSelector() => ".*"

      case CaseExpression(expression, alternatives, default) =>
        Seq(
          Seq("CASE"),
          for { e <- expression.toSeq; i <- Seq(inner(ast)(e)) } yield i,
          for { (e1, e2) <- alternatives; i <- Seq("WHEN", inner(ast)(e1), "THEN", inner(ast)(e2)) } yield i,
          for { e <- default.toSeq; i <- Seq("ELSE", inner(ast)(e)) } yield i,
          Seq("END")
        ).flatten.mkString(" ")

      case Ands(expressions) =>
        type ChainOp = Expression with ChainableBinaryOperatorExpression

        def findChain: Option[List[ChainOp]] = {
          val chainable = expressions.collect { case e: ChainableBinaryOperatorExpression => e }
          def allChainable = chainable.size == expressions.size
          def formsChain = chainable.sliding(2).forall(p => p.head.rhs == p.last.lhs)
          if (allChainable && formsChain) Some(chainable.toList) else None
        }

        findChain match {
          case Some(chain) =>
            val head = apply(chain.head)
            val tail = chain.tail.flatMap(o => List(o.canonicalOperatorSymbol, inner(ast)(o.rhs)))
            (head :: tail).mkString(" ")
          case None =>
            expressions.map(x => inner(ast)(x)).mkString(" AND ")
        }

      case AndedPropertyInequalities(_, _, exprs) =>
        exprs.map(apply).toIndexedSeq.mkString(" AND ")

      case Ors(expressions) =>
        expressions.map(x => inner(ast)(x)).mkString(" OR ")

      case ShortestPathExpression(pattern) =>
        patterns.apply(pattern)

      case PathExpression(pathStep) =>
        pathSteps(pathStep)

      case ReduceExpression(ReduceScope(Variable(acc), Variable(identifier), expression), init, list) =>
        val a = backtick(acc)
        val v = backtick(identifier)
        val i = inner(ast)(init)
        val l = inner(ast)(list)
        val e = inner(ast)(expression)
        s"reduce($a = $i, $v IN $l | $e)"

      case _: ExtractScope | _: FilterScope | _: ReduceScope =>
        // These are not really expressions, they are part of expressions
        ""

      case ExistsExpression(pat, where) =>
        val p = patterns.apply(pat)
        val w = where.map(wh => s" WHERE ${inner(ast)(wh)}").getOrElse("")
        s"EXISTS { MATCH $p$w }"

      case CountExpression(relChain, where) =>
        val p = patterns.apply(relChain)
        val w = where.map(wh => s" WHERE ${inner(ast)(wh)}").getOrElse("")
        s"COUNT { $p$w }"

      case UnaryAdd(r) =>
        val i = inner(ast)(r)
        s"+$i"

      case UnarySubtract(r) =>
        val i = inner(ast)(r)
        s"-$i"

      case CoerceTo(expr, typ) =>
        apply(expr)

      case CoerceToPredicate(expr) =>
        val inner = apply(expr)
        s"CoerceToPredicate($inner)"

      case AssertIsNode(argument) =>
        s"assertIsNode(${apply(argument)})"

      case _ =>
        extension(this)(ast)
    }
  }

  private def prettyScope(s: FilterScope, expression: Expression) = {
    Seq(
      for { i <- Seq(apply(s.variable), "IN", inner(s)(expression)) } yield i,
      for { p <- s.innerPredicate.toSeq; i <- Seq("WHERE", inner(s)(p)) } yield i
    ).flatten.mkString("(", " ", ")")
  }

  sealed trait Binding
  case object Syntactic extends Binding
  case class Precedence(level: Int) extends Binding

  private def binding(in: Expression): Binding = in match {
    case _: Or |
      _: Ors =>
      Precedence(12)

    case _: Xor =>
      Precedence(11)

    case _: And |
      _: Ands =>
      Precedence(10)

    case _: Not =>
      Precedence(9)

    case _: Equals |
      _: NotEquals |
      _: InvalidNotEquals |
      _: GreaterThan |
      _: GreaterThanOrEqual |
      _: LessThan |
      _: LessThanOrEqual =>
      Precedence(8)

    case _: Add |
      _: Subtract =>
      Precedence(7)

    case _: Multiply |
      _: Divide |
      _: Modulo =>
      Precedence(6)

    case _: Pow =>
      Precedence(5)

    case _: UnaryAdd |
      _: UnarySubtract =>
      Precedence(4)

    case _: RegexMatch |
      _: In |
      _: StartsWith |
      _: EndsWith |
      _: Contains |
      _: IsNull |
      _: IsNotNull =>
      Precedence(3)

    case _: Property |
      _: HasLabels |
      _: ContainerIndex |
      _: ListSlice =>
      Precedence(2)

    case _ =>
      Syntactic

  }

  override def backtick(txt: String): String = {
    ExpressionStringifier.backtick(txt, alwaysBacktick)
  }

  override def quote(txt: String): String = {
    val str = txt.replaceAll("\\\\", "\\\\\\\\")
    val containsSingle = str.contains('\'')
    val containsDouble = str.contains('"')
    if (containsDouble && containsSingle)
      "\"" + str.replaceAll("\"", "\\\\\"") + "\""
    else if (containsDouble || preferSingleQuotes)
      "'" + str + "'"
    else
      "\"" + str + "\""
  }

  override def escapePassword(password: Expression): String = password match {
    case _: SensitiveAutoParameter if !sensitiveParamsAsParams => "'******'"
    case _: SensitiveLiteral                                   => "'******'"
    case param: Parameter                                      => s"$$${ExpressionStringifier.backtick(param.name)}"
  }

  override def stringifyLabelExpression(labelExpression: LabelExpression): String = labelExpression match {
    case le: Disjunctions =>
      le.children.map(stringifyLabelExpressionHalfAtom).mkString("|")
    case le: ColonDisjunction =>
      s"${stringifyLabelExpressionInColonDisjunction(le.lhs)}|:${stringifyLabelExpressionHalfAtom(le.rhs)}"
    case le: Conjunctions =>
      le.children.map(stringifyLabelExpressionHalfAtom).mkString("&")
    case le: ColonConjunction =>
      s"${stringifyLabelExpressionInColonConjunction(le.lhs)}:${stringifyLabelExpressionHalfAtom(le.rhs)}"
    case le => s"${stringifyLabelExpressionHalfAtom(le)}"
  }

  private def stringifyLabelExpressionInColonDisjunction(labelExpression: LabelExpression): String =
    labelExpression match {
      case le: ColonDisjunction =>
        s"${stringifyLabelExpressionInColonDisjunction(le.lhs)}|:${stringifyLabelExpressionHalfAtom(le.rhs)}"
      case le => s"${stringifyLabelExpressionHalfAtom(le)}"
    }

  private def stringifyLabelExpressionInColonConjunction(labelExpression: LabelExpression): String =
    labelExpression match {
      case le: ColonConjunction =>
        s"${stringifyLabelExpressionInColonConjunction(le.lhs)}:${stringifyLabelExpressionHalfAtom(le.rhs)}"
      case le => s"${stringifyLabelExpressionHalfAtom(le)}"
    }

  private def stringifyLabelExpressionHalfAtom(labelExpression: LabelExpression): String = labelExpression match {
    case le: Negation => s"!${stringifyLabelExpressionHalfAtom(le.e)}"
    case le           => s"${stringifyLabelExpressionAtom(le)}"
  }

  private def stringifyLabelExpressionAtom(labelExpression: LabelExpression): String = labelExpression match {
    case Leaf(name)  => apply(name)
    case _: Wildcard => s"%"
    case le          => s"(${stringifyLabelExpression(le)})"
  }
}

object ExpressionStringifier {

  def apply(
    extension: ExpressionStringifier.Extension,
    alwaysParens: Boolean,
    alwaysBacktick: Boolean,
    preferSingleQuotes: Boolean,
    sensitiveParamsAsParams: Boolean
  ): ExpressionStringifier = new DefaultExpressionStringifier(
    extension,
    alwaysParens,
    alwaysBacktick,
    preferSingleQuotes,
    sensitiveParamsAsParams
  )

  def apply(
    extender: Expression => String = failingExtender,
    alwaysParens: Boolean = false,
    alwaysBacktick: Boolean = false,
    preferSingleQuotes: Boolean = false,
    sensitiveParamsAsParams: Boolean = false
  ): ExpressionStringifier = new DefaultExpressionStringifier(
    Extension.simple(extender),
    alwaysParens,
    alwaysBacktick,
    preferSingleQuotes,
    sensitiveParamsAsParams
  )

  /**
   * Generates pretty strings from expressions.
   */
  def pretty(onFailure: Expression => String): ExpressionStringifier = {
    new PrettyExpressionStringifier(ExpressionStringifier(onFailure))
  }

  trait Extension {
    def apply(ctx: ExpressionStringifier)(expression: Expression): String
  }

  object Extension {

    def simple(func: Expression => String): Extension = new Extension {
      def apply(ctx: ExpressionStringifier)(expression: Expression): String = func(expression)
    }
  }

  /*
   * Some strings (identifiers) were escaped with back-ticks to allow non-identifier characters
   * When printing these again, the knowledge of the back-ticks is lost, but the same test for
   * non-identifier characters can be used to recover that knowledge.
   */
  def backtick(txt: String, alwaysBacktick: Boolean = false, globbing: Boolean = false): String = {
    def escaped = txt.replaceAll("`", "``")
    def orGlobbedCharacter(p: Int) = globbing && (p == '*'.asInstanceOf[Int] || p == '?'.asInstanceOf[Int])

    if (alwaysBacktick)
      s"`$escaped`"
    else {
      val isJavaIdentifier =
        txt.codePoints().limit(1).allMatch(p =>
          (Character.isJavaIdentifierStart(p) && Character.getType(
            p
          ) != Character.CURRENCY_SYMBOL) || orGlobbedCharacter(p)
        ) &&
          txt.codePoints().skip(1).allMatch(p => Character.isJavaIdentifierPart(p) || orGlobbedCharacter(p))
      if (!isJavaIdentifier)
        s"`$escaped`"
      else
        txt
    }
  }

  val failingExtender: Expression => String =
    e => throw new IllegalStateException(s"failed to pretty print $e")
}

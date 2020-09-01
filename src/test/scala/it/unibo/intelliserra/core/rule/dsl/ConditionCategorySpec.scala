package it.unibo.intelliserra.core.rule.dsl

import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.SimpleConditionStatement
import it.unibo.intelliserra.core.sensor.Category
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConditionCategorySpec extends WordSpecLike with Matchers {

  case object Temperature extends Category
  private val temperatureValue = 20

  "A condition category" must {
    "create a statement with major operator" in {
      checkStatementOperator(Temperature > temperatureValue)(MajorOperator)
    }

    "create a statement with major equal operator" in {
      checkStatementOperator(Temperature >= temperatureValue)(MajorEqualsOperator)
    }

    "create a statement with minor operator" in {
      checkStatementOperator(Temperature < temperatureValue)(MinorOperator)
    }

    "create a statement with minor equal operator" in {
      checkStatementOperator(Temperature <= temperatureValue)(MinorEqualsOperator)
    }

    "create a statement with equal operator" in {
      checkStatementOperator(Temperature =:= temperatureValue)(EqualsOperator)
    }

    "create a statement with not equal operator" in {
      checkStatementOperator(Temperature =\= temperatureValue)(NotEqualsOperator)
    }
  }

  private def checkStatementOperator(statement: ConditionStatement)(operator: ConditionOperator): Unit = statement match {
    case SimpleConditionStatement(_, op, _) => op should be equals operator
    case _ => fail()
  }

}

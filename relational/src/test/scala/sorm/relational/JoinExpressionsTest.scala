package sorm.relational

import sorm.core._
import sorm.relational.joinExpressions._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.sql.{Types => jdbcTypes}

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class JoinExpressionsTest extends FunSuite with ShouldMatchers with joinExpressions.compilers.All {

  case class A(a: Int, b: Boolean)

  type Path1 = static.TypePath.Property[A, static.TypePath.Root[A], shapeless._0]
  type Path2 = static.TypePath.Property[A, static.TypePath.Root[A], shapeless.nat._1]
  type OutputTemplate = joinExpressions.templates.Where
  type OutputValues = List[Value]
  type Compiler[inputTemplate, inputValues] = expressions.Compiler[inputTemplate, inputValues, OutputTemplate, OutputValues]

  test("Int Equal Compiler") {

    type InputTemplate = expressions.templates.Where.Comparison[A, Path1, expressions.templates.Operator.Equal, util.typeLevel.Bool.True]
    type InputValues = expressions.values.Where.Comparison[expressions.values.Expression.Value[Int]]

    val inputTemplate : InputTemplate = {
      val path = null : Path1
      val operator = expressions.templates.Operator.Equal()
      val negative = util.typeLevel.Bool.True()
      expressions.templates.Where.Comparison(path, operator, negative)
    }
    val outputTemplate = {
      import joinExpressions.templates._
      val column = {
        val from = From.Root("a")
        Column("a", from)
      }
      val value = Expression.Placeholder
      val operator = Operator.Equal
      val negative = true
      Where.Comparison(column, value, operator, negative)
    }

    implicitly[Compiler[InputTemplate, InputValues]].compileTemplate(inputTemplate).shouldBe(outputTemplate)

  }

  test("Fork/Int/Boolean Compiler") {

    val inputTemplate = {
      import expressions.templates._
      val left = {
        val path = null: Path1
        val operator = Operator.Equal()
        val negative = util.typeLevel.Bool.True()
        Where.Comparison(path, operator, negative): Where.Comparison[A, path.type, operator.type, negative.type]
      }
      val right = {
        val path = null: Path2
        val operator = Operator.Equal()
        val negative = util.typeLevel.Bool.False()
        Where.Comparison(path, operator, negative): Where.Comparison[A, path.type, operator.type, negative.type]
      }
      val or = util.typeLevel.Bool.True()
      Where.Fork(left, right, or)
    }
    val inputValues = {
      import expressions.values._
      val left = Where.Comparison(Expression.Value(2))
      val right = Where.Comparison(Expression.Value(false))
      Where.Fork(left, right)
    }
    val outputTemplate = {
      import joinExpressions.templates._
      val left = {
        val column = Column("a", From.Root("a"))
        val value = Expression.Placeholder
        val operator = Operator.Equal
        val negative = true
        Where.Comparison(column, value, operator, negative)
      }
      val right = {
        val column = Column("b", From.Root("a"))
        val value = Expression.Placeholder
        val operator = Operator.Equal
        val negative = false
        Where.Comparison(column, value, operator, negative)
      }
      Where.Fork(left, right, true)
    }
    val outputValues = {
      val left = Value(2, jdbcTypes.INTEGER) :: Nil
      val right = Value(false, jdbcTypes.TINYINT) :: Nil
      left ++ right
    }

    val compiler = implicitly[Compiler[inputTemplate.type, inputValues.type]]

    compiler.compileTemplate(inputTemplate).shouldBe(outputTemplate)
    compiler.processValues(inputValues).shouldBe(outputValues)
  }

}

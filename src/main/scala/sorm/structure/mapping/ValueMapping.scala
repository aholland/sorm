package sorm.structure.mapping

import sorm._
import sext.Sext._
import reflection._
import ddl._
import structure._
import org.joda.time.{LocalDate, LocalTime, DateTime}

sealed class ValueMapping
  ( val membership : Option[Membership],
    val reflection : Reflection,
    settingsMap : SettingsMap )
  extends ColumnMapping
  {

    lazy val columnType
      = reflection match {
          case _ if reflection inheritsFrom Reflection.apply[String]
            ⇒ if (isKeyPart)
                Column.Type.VarChar
              else
                Column.Type.Text
          case _ if reflection inheritsFrom Reflection[BigDecimal]
            ⇒ Column.Type.Decimal
          case _ if reflection inheritsFrom Reflection[Boolean]
            ⇒ Column.Type.Boolean
          case _ if reflection inheritsFrom Reflection[Byte]
            ⇒ Column.Type.TinyInt
          case _ if reflection inheritsFrom Reflection[Short]
            ⇒ Column.Type.SmallInt
          case _ if reflection inheritsFrom Reflection[Int]
            ⇒ Column.Type.Integer
          case _ if reflection inheritsFrom Reflection[Long]
            ⇒ Column.Type.BigInt
          case _ if reflection inheritsFrom Reflection[Float]
            ⇒ Column.Type.Float
          case _ if reflection inheritsFrom Reflection[Double]
            ⇒ Column.Type.Double
          case _ if reflection inheritsFrom Reflection[DateTime]
            ⇒ Column.Type.TimeStamp
          case _ if reflection inheritsFrom Reflection[LocalTime]
            ⇒ Column.Type.Time
          case _ if reflection inheritsFrom Reflection[LocalDate]
            ⇒ Column.Type.Date
          case _ 
            ⇒ ???
        }

    lazy val autoIncremented
      = membership match {
          case Some(Membership.EntityId(_)) => true
          case _ => false
        }

  }
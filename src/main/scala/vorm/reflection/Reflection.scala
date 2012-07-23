package vorm.reflection

import reflect.mirror
import vorm._

sealed class Reflection 
  ( val t: mirror.Type,
    val javaClass: Class[_] )
  {
    lazy val fullName
      = mirrorQuirks.fullName(t.typeSymbol)

    lazy val name
      = mirrorQuirks.name(t.typeSymbol)

    lazy val properties
      : Map[String, Reflection]
      = mirrorQuirks.properties(t).view
          .map {
            s ⇒ mirrorQuirks.name(s) → 
                Reflection(s.typeSignature)
          }
          .toMap

    lazy val generics
      : IndexedSeq[Reflection]
      = mirrorQuirks.generics(t).view
          .map(Reflection(_))
          .toIndexedSeq

    def inheritsFrom
      ( reflection : Reflection )
      : Boolean
      = reflection.fullName match {
          case n if n == "scala.Any" 
            ⇒ true
          case n if n == "scala.AnyVal" 
            ⇒ t.typeSymbol.isPrimitiveValueClass
          case _ 
            ⇒ reflection.javaClass.isAssignableFrom(javaClass) &&
              generics.view
                .zip(reflection.generics)
                .forall {case (a, b) => a.inheritsFrom(b)}
        }

    lazy val constructorArguments
      : Map[String, Reflection]
      = mirrorQuirks.constructors(t)
          .head
          .typeSignature
          .asInstanceOf[{def params: List[mirror.Symbol]}]
          .params
          .map(s ⇒ mirrorQuirks.name(s) → Reflection(s.typeSignature) )
          .toMap


    def instantiate
      ( params : Map[String, Any] )
      : Any
      = instantiate( constructorArguments.keys.map(params) )

    def instantiate
      ( args : Traversable[Any] = Nil )
      : Any
      = {
        if ( mirrorQuirks.isInner(t.typeSymbol) )
          throw new UnsupportedOperationException(
              "Dynamic instantiation of inner classes is not supported"
            )
        
        javaClass.getConstructors.head
          .newInstance( args.asInstanceOf[Seq[Object]] : _* )
      }

  }
  
object Reflection {

  private val cache 
    = new collection.mutable.HashMap[( mirror.Type, Class[_] ), Reflection] {
        override def default
          ( key : ( mirror.Type, Class[_] ) ) 
          = {
            val value = new Reflection(key._1, key._2)
            update(key, value)
            value
          }
      }

  def apply
    [ T ]
    ( implicit tag : TypeTag[T] )
    : Reflection
    = cache( tag.tpe, tag.erasure )

  def apply
    ( mt : mirror.Type )
    : Reflection 
    = cache( mt, mirrorQuirks.javaClass( mt ) )

}
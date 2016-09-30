package object benderama {
  import shapeless._

  object queriable extends Poly1 {
    implicit def caseInt = at[Int](identity)
    implicit def caseString = at[String](a => s""""$a"""")
    implicit def caseBoolean = at[Boolean](identity)
  }

  type BenderType = String :+: Int :+: Boolean :+: CNil

  object BenderType {
    import ops.coproduct.Inject
    def apply[T](t: T)(implicit inj: Inject[BenderType, T]) = Coproduct[BenderType](t)
  }

  sealed trait BenderColumn {
    def isAutoIncrement: Boolean
  }

  case class BenderColumnInt(isAutoIncrement: Boolean, data: Seq[Int]) extends BenderColumn
  case class BenderColumnString(isAutoIncrement: Boolean, data: Seq[String]) extends BenderColumn
}

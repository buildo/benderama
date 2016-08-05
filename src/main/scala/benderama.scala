package benderama

import org.joda.time._
import scalikejdbc._
import scalikejdbc.config._

object BenderTypes {
  import shapeless._
  type BenderType = String :+: Int :+: Long :+: DateTime :+: CNil
  object BenderType {
    import ops.coproduct.Inject
    def apply[T](t: T)(implicit inj: Inject[BenderType, T]) = Coproduct[BenderType](t)
  }

  case class BenderColumn(columnName: String, columnType: String, autoIncrement: Boolean)

  sealed trait BenderColumnModel {
    def sample: BenderType
  }

  object BenderColumnModel {
    case class NormalModel(avg: Double, std: Double) extends
  }
}

object Data {
  import BenderTypes._

  def getTableColumns(table: String): List[(String, String, Boolean)] = DB readOnly { session =>
    val columns = DB.getTable(table) map (_.columns) getOrElse (throw new Exception("table not found"))
    columns map (c => (c.name, c.typeName, c.isAutoIncrement))
  }

  def mapJdbcType(columnType: String, columnName: String)(w: WrappedResultSet): BenderType = columnType match {
    case "TEXT" => BenderType(w.string(columnName))
    case "VARCHAR" => BenderType(w.string(columnName))
    case "INT" => BenderType(w.int(columnName))
    case "LONG" => BenderType(w.long(columnName))
    case "DATETIME" => BenderType(w.jodaDateTime(columnName))
    case other => throw new Exception(other)
  }

  def tableContent(columnName: String, columnType: String, tableName: String): Seq[BenderType] = {
    DB readOnly { implicit session =>
      SQL(s"select $columnName from $tableName").map(mapJdbcType(columnType, columnName)).list.apply
    }
  }

  def table(tableName: String): Map[BenderColumn, Seq[BenderType]] =
  getTableColumns(tableName).map { case (columnName, columnType, autoIncrement) =>
    (BenderColumn(columnName, columnType, autoIncrement) -> tableContent(columnName, columnType, tableName))
  }.toMap

  def insertRow(tableName: String, row: Map[String, BenderType]): Unit = DB localTx { implicit session =>
    val cols = row map(_._1) mkString (",")
    val vals = row map(_._2) mkString (",")
    SQL(s"INSERT INTO tbl_name ($cols) VALUES($vals)").update.apply
  }

}

object Model {
  import BenderTypes._
  def train(data: Map[BenderColumn, Seq[BenderType]]): Map[BenderColumn, BenderColumnModel] = ???
}

object Sampler {
  import BenderTypes._
  implicit class M(model: Map[BenderColumn, BenderColumnModel]) {
    def sample: Map[String, BenderType] = model map { case (k, v) => (k.columnName, v.sample) }
  }
}

object Main extends App {
  import BenderTypes._
  import Sampler._
  DBs.setupAll()

  import com.typesafe.config.ConfigFactory
  val conf = ConfigFactory.load()

  val tableName: String = conf.getString("benderama.table")
  val tableData: Map[BenderColumn, Seq[BenderType]] = Data.table(tableName)
  val tableDataModel: Map[BenderColumn, BenderColumnModel] = Model.train(tableData)

  import cronish._
  import cronish.dsl._

  val cron = conf.getString("benderama.insertionFrequency")

  val row: Map[String, BenderType] = tableDataModel.sample

  val insertion = task { Data.insertRow(tableName, row) }

  insertion executes cron

  DBs.closeAll()
}

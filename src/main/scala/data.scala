package benderama
import scalikejdbc._

package object data {
  def table(tableName: String): Map[String, BenderColumn] = DB readOnly { implicit session =>
    DB.getTable(tableName)
      .map(_.columns).getOrElse(throw new Exception("table not found"))
      .map { c =>
        val (columnName, columnType, isAutoIncrement) = (c.name, c.typeName, c.isAutoIncrement)
        val dataQuery = SQL(s"select $columnName from $tableName")
        val benderColumn = columnType match {
          case "TEXT" => BenderColumnString(isAutoIncrement, dataQuery.map(w => w.string(columnName)).list.apply)
          case "VARCHAR" => BenderColumnString(isAutoIncrement, dataQuery.map(w => w.string(columnName)).list.apply)
          case "INT" => BenderColumnInt(isAutoIncrement, dataQuery.map(w => w.int(columnName)).list.apply)
          //case "LONG" => dataQuery.map(w => w.long(columnName)).list.apply)
          //case "DATETIME" => BenderdataQuery.map(w =>w.jodaDateTime(columnName)).list.apply)
          case other => throw new Exception(other)
        }

        (columnName -> benderColumn)
      }.toMap
  }

  def insertRow(tableName: String, row: Map[String, BenderType]): Unit = DB localTx { implicit session =>
    val cols = row.keys.mkString(",")
    val vals = row.values.map(_.map(queriable).unify).toList
    SQL(s"INSERT INTO $tableName ($cols) VALUES(${vals.mkString(",")})").update.apply
  }
}

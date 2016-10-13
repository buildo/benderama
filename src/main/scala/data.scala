package benderama
import scalikejdbc._

package object data {
  val maxRows = 5000

  def table(tableName: String): Map[String, BenderColumn] = DB readOnly { implicit session =>
    DB.getTable(tableName)
      .map(_.columns).getOrElse(throw new Exception("table not found"))
      .map { c =>
        val (columnName, columnType, isAutoIncrement, isRequired) = (c.name, c.typeName, c.isAutoIncrement, c.isRequired)
        val q = SQL(s"select $columnName from $tableName limit $maxRows")
        val benderColumn = columnType match {
          case "TEXT" =>
            val d = if (isRequired) q.map(_.string(columnName)).list.apply
                    else q.map(_.stringOpt(columnName)).list.apply.flatten

            BenderColumnString(isAutoIncrement, isRequired, d)
          case "VARCHAR" =>
            val d = if (isRequired) q.map(_.string(columnName)).list.apply
                    else q.map(_.stringOpt(columnName)).list.apply.flatten

            BenderColumnString(isAutoIncrement, isRequired, d)
          case "INT" =>
            val d = if (isRequired) q.map(_.int(columnName)).list.apply
                    else q.map(_.intOpt(columnName)).list.apply.flatten

            BenderColumnInt(isAutoIncrement, isRequired, d)
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

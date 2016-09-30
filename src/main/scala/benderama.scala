package benderama

import data._
import models._
import sampler._

import scalikejdbc.config.DBs

import com.typesafe.config.ConfigFactory

object Main extends App {
  DBs.setupAll()

  val conf = ConfigFactory.load()

  val tableName: String = conf.getString("benderama.table")
  val tableData: Map[String, BenderColumn] = table(tableName)
    .filter { case (_ , v) => !v.isAutoIncrement }

  val models: Map[String, BenderModel] = tableData.train()

  val draw = models.draw

  insertRow(tableName, draw)

  DBs.closeAll()
}

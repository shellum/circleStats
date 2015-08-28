package db

import java.sql.Date

import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * Created by cameron.shellum on 8/27/15.
 */
case class User(id: Long, name: String, review_hash: String, results_hash: String, time: Date)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id")

  def name = column[String]("name")

  def review_hash = column[String]("review_hash")

  def results_hash = column[String]("results_hash")

  def time = column[Date]("time")

  def * = (id, name, review_hash, results_hash, time) <>(User.tupled, User.unapply _)
}

object B {
  def main(args: Array[String]): Unit = {
    val db = Database.forConfig("db")
    try {
      val users = TableQuery[UserTable]
      val action = users.map(_.name).result
      val result: Future[Seq[String]] = db.run(action)
      val sql = action.statements.head
      println("sql: " + sql)
      Await.result(result, 60.seconds)
      result.onSuccess { case s => println(s"Result: $s") }

    } finally db.close
  }
}
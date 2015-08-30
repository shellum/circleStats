package db

import java.sql.Date

import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * Created by cameron.shellum on 8/27/15.
 */
case class User(id: Option[Int]=None, name: String, reviewsHash: String, resultsHash: String, time: Option[Date]=None)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)

  def name = column[String]("name")

  def review_hash = column[String]("review_hash")

  def results_hash = column[String]("results_hash")

  def time = column[Date]("time",O.PrimaryKey,O.AutoInc)

  def * = (id.?, name, review_hash, results_hash, time.?) <>(User.tupled, User.unapply _)

}

object UserTableUtils {
  def main(args: Array[String]): Unit = {
    println(hashExists("asdf"))
    println(hashExists("asd4"))
    println(hashExists("asd3"))
  }

//  def hashExists(hash: String): Boolean = {
//    genericSlickCall((x:UserTable)=>{x.asInstanceOf[UserTable].review_hash === hash}, (x:Seq[UserTable])=> {x.asInstanceOf[Seq[UserTable]].size != 0}, classOf[UserTable])
//  }
//  def genericSlickCall[T,R,X](filter: (T)=>Rep[Boolean], compute: (Seq[T])=>R,c: Class[X]) = {
//    val db = Database.forConfig("db")
//    try {
//      val users = TableQuery[X]
//      val action = users.withFilter(filter).result
//      val result = db.run(action)
//      val sql = action.statements.head
//      println("sql: " + sql)
//      val list = Await.result(result, 10 seconds)
//      true//compute(list)
//
//    } finally db.close
//  }

  def hashExists(reviewsHash: String): Boolean = {
    val db = Database.forConfig("mydb")
    try {
      val users = TableQuery[UserTable]
      val action = users.withFilter(_.review_hash === reviewsHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.size != 0

    } finally db.close

  }

  def getResultsHash(reviewsHash: String): String = {
    val db = Database.forConfig("mydb")
    try {
      val users = TableQuery[UserTable]
      val action = users.withFilter(_.review_hash === reviewsHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list(0).resultsHash

    } finally db.close

  }


  def addUser(user: User) = {
    val db = Database.forConfig("mydb")
    try {
      val users = TableQuery[UserTable]
      val action = users += user
      val result = db.run(action)

     // val result = db.run(users += user)
      Await.result(result, 10 seconds)
    } finally db.close
  }
}
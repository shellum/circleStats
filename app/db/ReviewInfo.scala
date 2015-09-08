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
case class ReviewInfo(id: Option[Int]=None, name: String, reviewsHash: String, resultsHash: String, userId: Option[Int]=None,time: Option[Date]=None)

class ReviewInfoTable(tag: Tag) extends Table[ReviewInfo](tag, "review_info") {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)

  def name = column[String]("name")

  def review_hash = column[String]("review_hash")

  def results_hash = column[String]("results_hash")

  def user_id = column[Int]("user_id")

  def time = column[Date]("time",O.PrimaryKey,O.AutoInc)

  def * = (id.?, name, review_hash, results_hash, user_id.?, time.?) <>(ReviewInfo.tupled, ReviewInfo.unapply _)

}

object ReviewInfoTableUtils {
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
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.withFilter(_.review_hash === reviewsHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.size != 0

    } finally db.close

  }

  def getReviewInfoForUser(userId: Int): List[ReviewInfo] = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      // TODO: order by date?
      val action = reviewInfo.withFilter(_.user_id === userId).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.map((info => ReviewInfo(name=info.name,reviewsHash=info.reviewsHash, resultsHash=info.resultsHash))).toList

    } finally db.close
  }

  def getResultsHash(reviewsHash: String): String = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.withFilter(_.review_hash === reviewsHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list(0).resultsHash

    } finally db.close

  }

  def getName(reviewsHash: String): String = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.withFilter(_.review_hash === reviewsHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list(0).name

    } finally db.close

  }


  def addReviewInfo(reviewInfo: ReviewInfo) = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfos = TableQuery[ReviewInfoTable]
      val action = reviewInfos += reviewInfo
      val result = db.run(action)

     // val result = db.run(users += user)
      Await.result(result, 10 seconds)
    } finally db.close
  }
}
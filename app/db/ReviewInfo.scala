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
case class ReviewInfo(id: Option[Int]=None, name: String, reviewsHash: String, resultsHash: String, reviewerType: Int, userId: Option[Int]=None,time: Option[Date]=None)

class ReviewInfoTable(tag: Tag) extends Table[ReviewInfo](tag, "review_info") {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)

  def name = column[String]("name")

  def review_hash = column[String]("review_hash")

  def results_hash = column[String]("results_hash")

  def reviewer_type = column[Int]("reviewer_type")

  def user_id = column[Int]("user_id")

  def time = column[Date]("time",O.PrimaryKey,O.AutoInc)

  def * = (id.?, name, review_hash, results_hash, reviewer_type, user_id.?, time.?) <>(ReviewInfo.tupled, ReviewInfo.unapply _)

}

object ReviewInfoTableUtils {
  def main(args: Array[String]): Unit = {
    println(hashExists("asdf"))
    println(hashExists("asd4"))
    println(hashExists("asd3"))
  }

  def hashExists(reviewHash: String): Boolean = {
    var db = Database.forConfig("mydb")
    val checkReviewHash = try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.filter(_.review_hash === reviewHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.size == 0
    } finally db.close
    db = Database.forConfig("mydb")
    val checkResultsHash = try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.filter(_.results_hash === reviewHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.size == 0
    } finally db.close

    !(checkReviewHash && checkResultsHash)
  }

  def getReviewInfoForUser(userId: Int): List[ReviewInfo] = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      // TODO: order by date?
      val action = reviewInfo.withFilter(_.user_id === userId).sortBy(info => info.name)result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.map(info => ReviewInfo(name=info.name,reviewsHash=info.reviewsHash, resultsHash=info.resultsHash, reviewerType = info.reviewerType)).toList

    } finally db.close
  }

  def getResultsHash(reviewsHash: String): ReviewInfo = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.withFilter(_.review_hash === reviewsHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list(0)

    } finally db.close

  }

  def getNameFromReviewsHash(f: String => Boolean): String = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.withFilter(f).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list(0).name

    } finally db.close

  }

  def getNameFromResultsHash(resultsHash: String): String = {
    val db = Database.forConfig("mydb")
    try {
      val reviewInfo = TableQuery[ReviewInfoTable]
      val action = reviewInfo.withFilter(_.results_hash === resultsHash).result
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
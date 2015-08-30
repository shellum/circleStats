package db
import java.sql.Date

import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
/**
 * Created by cameron.shellum on 8/29/15.
 */
case class Review(id: Option[Int]=None, reviewsHash: String, resultsHash: String, attribute: String, score: Int, reviewerType: Int, time: Option[Date]=None)

class ReviewTable(tag: Tag) extends Table[Review](tag, "reviews") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def reviewsHash = column[String]("reviews_hash")
  def resultsHash = column[String]("results_hash")
  def attribute = column[String]("attribute")
  def score = column[Int]("score")
  def reviewerType = column[Int]("reviewer_type")
  def time = column[Date]("time", O.PrimaryKey, O.AutoInc)

  def * = (id.?, reviewsHash, resultsHash, attribute, score, reviewerType, time.?) <>(Review.tupled, Review.unapply _)
}

object ReviewTableUtils {
  def addReview(review: Review) = {
    val db = Database.forConfig("mydb")
    try {
      val reviews = TableQuery[ReviewTable]
      val action = reviews += review
      val result = db.run(action)

      Await.result(result, 10 seconds)
    } finally db.close
  }
}
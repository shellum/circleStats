package db
import java.sql.Date

import controllers.Const
import play.api.libs.json.{JsPath, Writes, Json}
import slick.driver.PostgresDriver.api._
import play.api.libs.json.{Writes, JsPath}
import play.api.libs.json._
import play.api.libs.functional.syntax._
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

  def getReviews(resultsHash: String, attribute: String): String = {
    val db = Database.forConfig("mydb")
    try {
      val results = TableQuery[ReviewTable]
      val action = results.withFilter(_.resultsHash === resultsHash).withFilter(_.attribute === attribute).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      var peerScores: List[Int] = List()
      var selfScore: Option[Int] = None
      var managerScore: Option[Int] = None
      var id = -1
      list.foreach((i: Review) => {
        i.reviewerType match {
          case Const.REVIEWER_TYPE_PEER => peerScores = i.score :: peerScores
          case Const.REVIEWER_TYPE_SELF => selfScore = Some(i.score)
          case Const.REVIEWER_TYPE_MANAGER => managerScore = Some(i.score)
        }
        id = i.id.get
      })
      var json = "{id:"+id+",title:\""+attribute+"\""
      if (peerScores.length > 0)
        json += ",peerScores:"+Json.toJson(peerScores)
      if (selfScore != None)
        json += ",selfScore:"+selfScore.get
      if (managerScore != None)
        json += ", managerScore:"+managerScore.get
      json += "},"
      json
    } finally db.close

  }

}

case class Results(id: Int, name: String, peerScores: List[Int], managerScore: Option[Int], selfScore: Option[Int])
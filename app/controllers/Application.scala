package controllers

import db.{Review, ReviewTableUtils, UserTableUtils, User}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import utils.Hash

class Application extends Controller {

  val attributes = Array("awesomeness", "coolness", "yep")

  def index = Action {
    Ok(views.html.index())
  }

  def setupReview = Action {
    Ok(views.html.setupReview())
  }

  def getHash = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    var revHash = ""
    var resHash = ""
    // TODO: make check & write atomic
    do {
      revHash = Hash.createHash(16)
      resHash = Hash.createHash(16)
    } while(UserTableUtils.hashExists(revHash))
    UserTableUtils.addUser(User(name=formData.name,reviewsHash=revHash, resultsHash = resHash))
    // TODO: make sure these hashes are not reserved, and reserve them or loop
    val map = Map("name" -> formData.name, "reviewsHash" -> revHash, "resultsHash" -> resHash)
    Ok(Json.toJson(map))
  }

  def review(reviewsHash: String) = Action {
    Ok(views.html.review(reviewsHash, attributes))
  }

  def save(reviewsHash: String) = Action { implicit request =>
    val formData = reviewScoresForm.bindFromRequest.get
    val resultsHash = UserTableUtils.getResultsHash(reviewsHash)
    val attributes = Map(
      "awesomeness" -> formData.awesomeness,
      "coolness" -> formData.coolness,
      "yep" -> formData.yep
    )
    for((attribute, value) <- attributes)
      ReviewTableUtils.addReview(Review(reviewsHash = reviewsHash, resultsHash = resultsHash, attribute = attribute, score = value, reviewerType = formData.reviewer_type))

    // TODO: save review data
    //
    // person table
    // review_hash, results_hash, name, timestamp
    //
    // reviews table
    // results_hash, reviewer_type(self, mgr, peer), attribute,  results(as json), timestamp
    // asdf,         1,                              awesomeness,5
    // asdf,         1,                              awesomeness,4
    // asdf,         1,                              coolness   ,6
    Ok(views.html.save())
  }

  val userForm = Form(
    mapping(
      "name" -> text
    )(UserForm.apply)(UserForm.unapply)
  )

  val reviewScoresForm = Form(
    mapping(
      "reviewerType" -> number,
      "awesomeness" -> number,
      "coolness" -> number,
      "yep" -> number,
      "reviewsHash" -> text
    )(ReviewScoresForm.apply)(ReviewScoresForm.unapply)
  )

}

object Const {
  val REVIEWER_TYPE_SELF = 0
  val REVIEWER_TYPE_MANAGER = 1
  val REVIEWER_TYPE_PEER = 2
}

case class UserForm(name: String)
case class ReviewScoresForm(reviewer_type: Int, awesomeness: Int, coolness: Int, yep: Int, reviewsHash: String)
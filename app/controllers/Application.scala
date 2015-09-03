package controllers

import db.{Review, ReviewTableUtils, UserTableUtils, User}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import utils.Hash

import views.html._

class Application extends Controller {

  val attributes = Array("awesomeness", "coolness", "sweetness")

  def index = Action {
    Ok(views.html.index())
  }

  def results(resultsHash: String) = Action {
    var jsonData = ""
    // TODO: change to map reduce
    attributes.foreach(
      jsonData += ReviewTableUtils.getReviews(resultsHash, _)
    )
    Ok(views.html.results(jsonData))
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
      revHash = Hash.createHash(24)
      resHash = Hash.createHash(24)
    } while(UserTableUtils.hashExists(revHash))
    UserTableUtils.addUser(User(name=formData.name,reviewsHash=revHash, resultsHash = resHash))
    val map = Map("name" -> formData.name, "reviewsHash" -> revHash, "resultsHash" -> resHash)
    Ok(Json.toJson(map))
  }

  def review(reviewsHash: String) = Action {
    val name = UserTableUtils.getName(reviewsHash)
    Ok(views.html.review(reviewsHash, name, attributes))
  }

  def save(reviewsHash: String) = Action { implicit request =>
    val resultsHash = UserTableUtils.getResultsHash(reviewsHash)
    val reviewerType = request.body.asFormUrlEncoded.get("reviewerType")(0).toInt
    attributes.foreach((attribute: String)=> {
      val score = request.body.asFormUrlEncoded.get(attribute)(0).toInt
      ReviewTableUtils.addReview(Review(reviewsHash = reviewsHash, resultsHash = resultsHash, attribute = attribute, score = score, reviewerType = reviewerType))
    })
    Ok(views.html.save())
  }

  val userForm = Form(
    mapping(
      "name" -> text
    )(UserForm.apply)(UserForm.unapply)
  )

}

object Const {
  val REVIEWER_TYPE_SELF = 0
  val REVIEWER_TYPE_MANAGER = 1
  val REVIEWER_TYPE_PEER = 2
}

case class UserForm(name: String)

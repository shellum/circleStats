package controllers

import db._
import org.mindrot.jbcrypt.BCrypt
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import utils.Hash

import views.html._

class Application extends Controller {

  val attributes = Array("awesomeness", "coolness", "sweetness")

  def index = Action { implicit request =>
    val passwordHash = request.cookies.get("login") match {
      case Some(c) => Option(c.value)
      case None => None
    }
    val email = UserTableUtils.getEmail(passwordHash)
    Ok(views.html.index(email))
  }

  def thanks = Action {
    Ok(views.html.save())
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
    val formData = reviewInfoForm.bindFromRequest.get
    val passwordHash = request.cookies.get("login") match {
      case Some(cookie) => Option(cookie.value)
      case None => None
    }

    val userId = UserTableUtils.getUserId(passwordHash)

    var revHash = ""
    var resHash = ""
    // TODO: make check & write atomic
    do {
      revHash = Hash.createHash(24)
      resHash = Hash.createHash(24)
    } while(ReviewInfoTableUtils.hashExists(revHash))
    ReviewInfoTableUtils.addReviewInfo(ReviewInfo(name=formData.name,reviewsHash=revHash, resultsHash = resHash,userId = userId))
    val map = Map("name" -> formData.name, "reviewsHash" -> revHash, "resultsHash" -> resHash)
    Ok(Json.toJson(map))
  }

  def review(reviewsHash: String) = Action {
    val name = ReviewInfoTableUtils.getName(reviewsHash)
    Ok(views.html.review(reviewsHash, name, attributes))
  }

  def join() = Action {
    Ok(views.html.join())
  }

  def commitJoin() = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    if (UserTableUtils.emailExists(formData.email)) {
      val map = Map("emailExists" -> true)
      Ok(Json.toJson(map))
    }
    else {
      val bcryptHash: String = BCrypt.hashpw(formData.passwordHash, BCrypt.gensalt());
      val user = User(email=formData.email, passwordHash=bcryptHash)
      UserTableUtils.addUser(user)
      val map = Map("emailExists" -> false)
      Ok(Json.toJson(map)).withCookies(Cookie("login",bcryptHash))
    }
  }

  def signIn() = Action {
    Ok(views.html.signIn())
  }

  def signInCheck() = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    val bcryptHash: String = BCrypt.hashpw(formData.passwordHash, BCrypt.gensalt());
    if (UserTableUtils.loginOkay(formData.email, bcryptHash)) {
      val map = Map("badLogin" -> false)
      Ok(Json.toJson(map)).withCookies(Cookie("login",bcryptHash))
    }
    else {
      val user = User(email=formData.email, passwordHash=formData.passwordHash)
      val map = Map("badLogin" -> true)
      Ok(Json.toJson(map))
    }
  }


  def save(reviewsHash: String) = Action { implicit request =>
    val resultsHash = ReviewInfoTableUtils.getResultsHash(reviewsHash)
    val reviewerType = request.body.asFormUrlEncoded.get("reviewerType")(0).toInt
    attributes.foreach((attribute: String)=> {
      val score = request.body.asFormUrlEncoded.get(attribute)(0).toInt
      ReviewTableUtils.addReview(Review(reviewsHash = reviewsHash, resultsHash = resultsHash, attribute = attribute, score = score, reviewerType = reviewerType))
    })
    Ok("")
  }

  val reviewInfoForm = Form(
    mapping(
      "name" -> text
    )(ReviewInfoForm.apply)(ReviewInfoForm.unapply)
  )

  val userForm = Form(
    mapping(
      "email" -> text,
      "passwordHash" -> text
    )(UserForm.apply)(UserForm.unapply)
  )

}

object Const {
  val REVIEWER_TYPE_SELF = 0
  val REVIEWER_TYPE_MANAGER = 1
  val REVIEWER_TYPE_PEER = 2
}

case class ReviewInfoForm(name: String)
case class UserForm(email: String, passwordHash: String)
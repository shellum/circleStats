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

  def reviews = Action { implicit request =>
    val passwordHash = request.cookies.get("login") match {
      case Some(cookie) => Option(cookie.value)
      case None => None
    }

    val user = UserTableUtils.getUser(passwordHash)
    var reviews: List[ReviewInfo] = List()
    user match {
      case Some(u) => u.id match {
        case Some(id) => reviews = ReviewInfoTableUtils.getReviewInfoForUser(id)
        case None => reviews = List()
      }
      case None => reviews = List()
    }
    val email = UserTableUtils.getEmail(passwordHash)
    Ok(views.html.reviews(email, reviews))
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

    val user = UserTableUtils.getUser(passwordHash)

    var revHash = ""
    var resHash = ""
    // TODO: make check & write atomic
    do {
      revHash = Hash.createHash(24)
      resHash = Hash.createHash(24)
    } while(ReviewInfoTableUtils.hashExists(revHash))
    val userId = user match {
      case Some(u) => u.id
      case None => None
    }
    ReviewInfoTableUtils.addReviewInfo(ReviewInfo(name=formData.name,reviewsHash=revHash, resultsHash = resHash,userId = userId))
    val map = Map("name" -> formData.name, "reviewsHash" -> revHash, "resultsHash" -> resHash)
    Ok(Json.toJson(map))
  }

  def review(reviewsHash: String) = Action {
    val name = ReviewInfoTableUtils.getName(reviewsHash)
    Ok(views.html.review(reviewsHash, name, attributes))
  }

  def updateProfile() = Action { implicit request =>
    val passwordHash = request.cookies.get("login") match {
      case Some(cookie) => Option(cookie.value)
      case None => None
    }

    val user = UserTableUtils.getUser(passwordHash)
    val email = user match {
      case Some(u) => u.email
      case None => ""
    }
    Ok(views.html.profile(email))
  }

  def commitProfile() = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    val passwordHash = request.cookies.get("login") match {
      case Some(cookie) => Option(cookie.value)
      case None => None
    }

    val user = UserTableUtils.getUser(passwordHash)

    var bcryptHash = ""

    user match {
      case Some(u) => formData.passwordHash.isEmpty match {
        case true => bcryptHash = u.passwordHash
          val updatedUser = User(id = u.id, passwordHash = u.passwordHash, email = formData.email, time = u.time)
          UserTableUtils.updateUser(updatedUser)
        case false => bcryptHash = BCrypt.hashpw(formData.passwordHash, BCrypt.gensalt());
          val updatedUser = User(id = u.id, passwordHash = bcryptHash, email = formData.email, time = u.time)
          UserTableUtils.updateUser(updatedUser)
      }
      Map("emailExists" -> false)

      case None =>
        if (UserTableUtils.emailExists(formData.email)) {
          Map("emailExists" -> false)
        }
        else {
          bcryptHash = BCrypt.hashpw(formData.passwordHash, BCrypt.gensalt());
          val user = User(email=formData.email, passwordHash=bcryptHash)
          UserTableUtils.addUser(user)
        }
    }

    val map = Map("emailExists" -> false)
    Ok(Json.toJson(map)).withCookies(Cookie("login",bcryptHash))

  }

  def signIn() = Action {
    Ok(views.html.signIn())
  }

  def signInCheck() = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    val bcryptHash = UserTableUtils.getPasswordHashFromEmail(formData.email)
    if (BCrypt.checkpw(formData.passwordHash,bcryptHash)) {
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
package controllers

import db._
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import utils.{ControllerUtil, Hash}

class Application extends Controller {

  val attributes = Array("awesomeness", "coolness", "sweetness")
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

  def index = Action { implicit request =>
    val email = ControllerUtil.getEmailFromCookies(request)
    Ok(views.html.index(email))
  }

  def thanks = Action {
    Ok(views.html.save())
  }

  def results(resultsHash: String) = Action { implicit request =>
    val email = ControllerUtil.getEmailFromCookies(request)

    var jsonData = ""
    // TODO: change to map reduce
    attributes.foreach(
      jsonData += ReviewTableUtils.getReviews(resultsHash, _)
    )
    Ok(views.html.results(email, jsonData))
  }

  def reviews = Action { implicit request =>
    val user = ControllerUtil.getUserFromCookies(request)
    val passwordHash = ControllerUtil.getPasswordHashFromCookies(request)
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

  def getHash = Action { implicit request =>
    val formData = reviewInfoForm.bindFromRequest.get
    val user = ControllerUtil.getUserFromCookies(request)

    var revHash = ""
    var resHash = ""
    // TODO: make check & write atomic
    do {
      revHash = Hash.createHash(24)
      resHash = Hash.createHash(24)
    } while (ReviewInfoTableUtils.hashExists(revHash))
    val userId = user match {
      case Some(u) => u.id
      case None => None
    }
    ReviewInfoTableUtils.addReviewInfo(ReviewInfo(name = formData.name, reviewsHash = revHash, resultsHash = resHash, userId = userId))
    val map = Map("name" -> formData.name, "reviewsHash" -> revHash, "resultsHash" -> resHash)
    Ok(Json.toJson(map))
  }

  def review(reviewsHash: String) = Action { implicit request =>
    val email = ControllerUtil.getEmailFromCookies(request)

    val name = ReviewInfoTableUtils.getName(reviewsHash)
    Ok(views.html.review(email, reviewsHash, name, attributes))
  }

  def updateProfile() = Action { implicit request =>
    val user = ControllerUtil.getUserFromCookies(request)
    var emailOpt: Option[String] = None
    val email = user match {
      case Some(u) => emailOpt = Option(u.email)
        u.email
      case None => ""
    }
    Ok(views.html.profile(emailOpt, email))
  }

  def commitProfile() = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    val user = ControllerUtil.getUserFromCookies(request)

    var map: Map[String, Boolean] = Map()
    var bcryptHash = ""

    user match {
      case Some(u) => formData.passwordHash.isEmpty match {
        case true => bcryptHash = u.passwordHash
        case false => bcryptHash = BCrypt.hashpw(formData.passwordHash, BCrypt.gensalt());
      }
        if (UserTableUtils.emailExists(formData.email, u.id)) {
          map = Map("emailExists" -> true)
        }
        else {
          val updatedUser = User(id = u.id, passwordHash = bcryptHash, email = formData.email, time = u.time)
          UserTableUtils.updateUser(updatedUser)
          map = Map("emailExists" -> false)
        }

      case None =>
        if (UserTableUtils.emailExists(formData.email, None)) {
          map = Map("emailExists" -> true)
        }
        else {
          bcryptHash = BCrypt.hashpw(formData.passwordHash, BCrypt.gensalt());
          val user = User(email = formData.email, passwordHash = bcryptHash)
          UserTableUtils.addUser(user)
          map = Map("emailExists" -> false)
        }
    }

    Ok(Json.toJson(map)).withCookies(Cookie("login", bcryptHash))

  }

  def signIn() = Action {
    Ok(views.html.signIn())
  }

  def signInCheck() = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    val bcryptHash = UserTableUtils.getPasswordHashFromEmail(formData.email)
    if (BCrypt.checkpw(formData.passwordHash, bcryptHash)) {
      val map = Map("badLogin" -> false)
      Ok(Json.toJson(map)).withCookies(Cookie("login", bcryptHash))
    }
    else {
      val user = User(email = formData.email, passwordHash = formData.passwordHash)
      val map = Map("badLogin" -> true)
      Ok(Json.toJson(map))
    }
  }

  def save(reviewsHash: String) = Action { implicit request =>
    val resultsHash = ReviewInfoTableUtils.getResultsHash(reviewsHash)
    val reviewerType = request.body.asFormUrlEncoded.get("reviewerType")(0).toInt
    attributes.foreach((attribute: String) => {
      val score = request.body.asFormUrlEncoded.get(attribute)(0).toInt
      ReviewTableUtils.addReview(Review(reviewsHash = reviewsHash, resultsHash = resultsHash, attribute = attribute, score = score, reviewerType = reviewerType))
    })
    Ok("")
  }

}

object Const {
  val REVIEWER_TYPE_SELF = 0
  val REVIEWER_TYPE_MANAGER = 1
  val REVIEWER_TYPE_PEER = 2
}

case class ReviewInfoForm(name: String)

case class UserForm(email: String, passwordHash: String)
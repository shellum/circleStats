package controllers

import db._
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import utils.{ControllerUtil, Hash}

import scala.collection.parallel.mutable

class Application extends Controller {

  val attributes = Array("Shows Empathy", "Controls Impulses", "Socially Responsible", "Problem Solver", "Technically Capable", "Helpful")
  val reviewInfoForm = Form(
    mapping(
      "name" -> text
    )(ReviewInfoForm.apply)(ReviewInfoForm.unapply)
  )
  val userForm = Form(
    mapping(
      "username" -> text,
      "email" -> text,
      "passwordHash" -> text
    )(UserForm.apply)(UserForm.unapply)
  )

  val forgotPasswordForm = Form(
    mapping(
      "email" -> text
    )(ForgotPasswordForm.apply)(ForgotPasswordForm.unapply)
  )

  def index = Action { implicit request =>
    val username = ControllerUtil.getUsernameFromCookies(request)
    username match {
      case Some(u) => Redirect("/reviews")
      case _ => Ok(views.html.index(username))
    }
  }

  def thanks = Action { implicit request =>
    val username = ControllerUtil.getUsernameFromCookies(request)
    Ok(views.html.save(username))
  }

  def results(resultsHash: String) = Action { implicit request =>
    val username = ControllerUtil.getUsernameFromCookies(request)
    val reviewUser = ReviewInfoTableUtils.getNameFromResultsHash(resultsHash)
    var jsonData = ""
    // TODO: change to map reduce
    attributes.foreach(
      jsonData += ReviewTableUtils.getReviews(resultsHash, _)
    )
    Ok(views.html.results(username, reviewUser, jsonData))
  }

  def reviews = Action { implicit request =>
    val user = ControllerUtil.getUserFromCookies(request)
    val passwordHash = ControllerUtil.getPasswordHashFromCookies(request)
    var reviews: List[ReviewInfo] = List()
    var collapsedReviewsMap = scala.collection.mutable.Map[String, Collapsed]()
    user match {
      case Some(u) => u.id match {
        case Some(id) => reviews = ReviewInfoTableUtils.getReviewInfoForUser(id)
          reviews.foreach(r => {
            val collapsed = collapsedReviewsMap.getOrElse(r.resultsHash,new Collapsed())
            collapsed.name = r.name
            collapsed.resultsHash = r.resultsHash
            r.reviewerType match {
              case Const.REVIEWER_TYPE_PEER => collapsed.peerHash = r.reviewsHash
              case Const.REVIEWER_TYPE_SELF => collapsed.selfHash = r.reviewsHash
              case Const.REVIEWER_TYPE_MANAGER => collapsed.managerHash = r.reviewsHash
            }
            collapsedReviewsMap.put(r.resultsHash, collapsed)
          })
        case None => reviews = List()
      }
      case None => reviews = List()
    }
    val username = UserTableUtils.getUsername(passwordHash)
    val collapsedList = collapsedReviewsMap.values.toList
    Ok(views.html.reviews(username, collapsedList))
  }



  def getHash = Action { implicit request =>
    val formData = reviewInfoForm.bindFromRequest.get
    val user = ControllerUtil.getUserFromCookies(request)

    var peerReviewHash = ""
    var selfReviewHash = ""
    var managerReviewHash = ""
    var resultsHash = ""
    // TODO: make check & write atomic
    do { peerReviewHash = Hash.createHash(32)} while (ReviewInfoTableUtils.hashExists(peerReviewHash))
    do { selfReviewHash = Hash.createHash(32)} while (ReviewInfoTableUtils.hashExists(selfReviewHash))
    do { managerReviewHash = Hash.createHash(32)} while (ReviewInfoTableUtils.hashExists(managerReviewHash))
    do { resultsHash = Hash.createHash(32)} while (ReviewInfoTableUtils.hashExists(resultsHash))
    val userId = user match {
      case Some(u) => u.id
      case None => None
    }
    ReviewInfoTableUtils.addReviewInfo(ReviewInfo(name = formData.name, reviewsHash = peerReviewHash, reviewerType = Const.REVIEWER_TYPE_PEER, resultsHash = resultsHash, userId = userId))
    ReviewInfoTableUtils.addReviewInfo(ReviewInfo(name = formData.name, reviewsHash = selfReviewHash, reviewerType = Const.REVIEWER_TYPE_SELF, resultsHash = resultsHash, userId = userId))
    ReviewInfoTableUtils.addReviewInfo(ReviewInfo(name = formData.name, reviewsHash = managerReviewHash, reviewerType = Const.REVIEWER_TYPE_MANAGER, resultsHash = resultsHash, userId = userId))
    val map = Map("name" -> formData.name, "peerReviewHash" -> peerReviewHash, "selfReviewHash" -> selfReviewHash, "managerReviewHash" -> managerReviewHash, "resultsHash" -> resultsHash)
    Ok(Json.toJson(map))
  }

  def review(reviewsHash: String) = Action { implicit request =>
    val username = ControllerUtil.getUsernameFromCookies(request)

    val name = ReviewInfoTableUtils.getNameFromReviewsHash(reviewsHash)
    Ok(views.html.review(username, reviewsHash, name, attributes))
  }

  def updateProfile() = Action { implicit request =>
    val user = ControllerUtil.getUserFromCookies(request)
    var emailOpt: Option[String] = None
    var usernameOpt: Option[String] = None
    val email = user match {
      case Some(u) => emailOpt = Option(u.email)
        u.email
      case None => ""
    }
    val username = user match {
      case Some(u) => usernameOpt = Option(u.username)
        u.username
      case None => ""
    }
    Ok(views.html.profile(usernameOpt, email, username))
  }

  def forgotPassword(tmpHash: String) = Action { implicit request =>
    val user = UserTableUtils.getUserFromForgotPasswordHash(Some(tmpHash))
    var emailOpt: Option[String] = None
    var usernameOpt: Option[String] = None
    var bcryptHash = ""
    val email = user match {
      case Some(u) => emailOpt = Option(u.email)
        bcryptHash = u.passwordHash
        u.email
      case None => ""
    }
    val username = user match {
      case Some(u) => usernameOpt = Option(u.username)
        u.username
      case None => ""
    }
    Ok(views.html.profile(usernameOpt, email, username)).withCookies(Cookie("login", bcryptHash))
  }

  def lostPassword() = Action {
    Ok(views.html.forgot())
  }

  def sendLostPasswordLink() = Action { implicit request =>
    val formData = forgotPasswordForm.bindFromRequest.get
    val user = UserTableUtils.getUserFromEmail(formData.email)
    val forgotPasswordHash = UserTableUtils.addForgotPasswordHash(user.get)
    Hash.createSignature(formData.email, forgotPasswordHash, request.secure, request.host)
    Ok("")
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
        if (UserTableUtils.usernameExists(formData.username, u.id)) {
          map = Map("usernameExists" -> true)
        }
        else {
          val updatedUser = User(id = u.id, username = formData.username, passwordHash = bcryptHash, email = formData.email, time = u.time)
          UserTableUtils.updateUser(updatedUser)
          map = Map("usernameExists" -> false)
        }

      case None =>
        if (UserTableUtils.usernameExists(formData.username, None)) {
          map = Map("usernameExists" -> true)
        }
        else {
          bcryptHash = BCrypt.hashpw(formData.passwordHash, BCrypt.gensalt());
          val user = User(username = formData.username, email = formData.email, passwordHash = bcryptHash)
          UserTableUtils.addUser(user)
          map = Map("usernameExists" -> false)
        }
    }

    Ok(Json.toJson(map)).withCookies(Cookie("login", bcryptHash))

  }

  def signIn() = Action {
    Ok(views.html.signIn())
  }

  def signInCheck() = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    val bcryptHash = UserTableUtils.getPasswordHashFromUsername(formData.username)
    if (!bcryptHash.isEmpty && BCrypt.checkpw(formData.passwordHash, bcryptHash)) {
      val map = Map("badLogin" -> false)
      Ok(Json.toJson(map)).withCookies(Cookie("login", bcryptHash))
    }
    else {
      val map = Map("badLogin" -> true)
      Ok(Json.toJson(map))
    }
  }

  def save(reviewsHash: String) = Action { implicit request =>
    val reviewInfo = ReviewInfoTableUtils.getResultsHash(reviewsHash)
    attributes.foreach((attribute: String) => {
      val score = request.body.asFormUrlEncoded.get(attribute)(0).toInt
      ReviewTableUtils.addReview(Review(reviewInfoId = reviewInfo.id.get, resultsHash = reviewInfo.resultsHash, attribute = attribute, score = score, reviewerType = reviewInfo.reviewerType))
    })
    Ok("")
  }

  def signOut = Action { implicit request =>
    Ok(views.html.index(None)).discardingCookies(DiscardingCookie("login"))
  }

}

object Const {
  val REVIEWER_TYPE_SELF = 0
  val REVIEWER_TYPE_MANAGER = 1
  val REVIEWER_TYPE_PEER = 2
}

case class ReviewInfoForm(name: String)
case class ForgotPasswordForm(email: String)
case class UserForm(username: String, email: String, passwordHash: String)

class Collapsed() {
  var name: String=""
  var peerHash: String=""
  var selfHash: String=""
  var managerHash: String=""
  var resultsHash: String=""
}
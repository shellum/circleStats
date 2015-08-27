package controllers

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
    val map = Map("name" -> formData.name, "reviewHash" -> Hash.createHash(16), "resultsHash" -> Hash.createHash(16))
    Ok(Json.toJson(map))
  }

  def review(hash: String) = Action {
    Ok(views.html.review(hash, attributes))
  }

  def save(hash: String) = Action { implicit request =>
    val formData = reviewScoresForm.bindFromRequest.get
    Ok(views.html.save())
  }

  val userForm = Form(
    mapping(
      "name" -> text
    )(User.apply)(User.unapply)
  )

  val reviewScoresForm = Form(
    mapping(
      "awesomeness" -> number,
      "coolness" -> number,
      "yep" -> number
    )(ReviewScores.apply)(ReviewScores.unapply)
  )

}

case class User(name: String)
case class ReviewScores(awesomeness: Int, coolness: Int, yep: Int)
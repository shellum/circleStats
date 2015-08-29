package controllers

import db.UserTableUtils
import db.User
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
    UserTableUtils.addUser(User(name=formData.name,reviewHash=revHash, resultsHash = resHash))
    // TODO: make sure these hashes are not reserved, and reserve them or loop
    val map = Map("name" -> formData.name, "reviewHash" -> revHash, "resultsHash" -> resHash)
    Ok(Json.toJson(map))
  }

  def review(hash: String) = Action {
    Ok(views.html.review(hash, attributes))
  }

  def save(hash: String) = Action { implicit request =>
    val formData = reviewScoresForm.bindFromRequest.get
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
      "awesomeness" -> number,
      "coolness" -> number,
      "yep" -> number
    )(ReviewScoresForm.apply)(ReviewScoresForm.unapply)
  )

}

case class UserForm(name: String)
case class ReviewScoresForm(awesomeness: Int, coolness: Int, yep: Int)
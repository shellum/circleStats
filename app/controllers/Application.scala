package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def setupReview = Action {
    Ok(views.html.setupReview())
  }

  def getHash = Action { implicit request =>
    val formData = userForm.bindFromRequest.get
    Ok(formData.name + "23f4se5g")
  }

  def review(hash: String) = Action {
    Ok(views.html.review(hash))
  }

  def save(hash: String) = Action { request =>
    Ok(views.html.save())
  }

  val userForm = Form(
    mapping(
      "name" -> text
    )(User.apply)(User.unapply)
  )

}

case class User(name: String)
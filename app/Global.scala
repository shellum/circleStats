/**
 * Created by cameron.shellum on 9/17/15.
 */

import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(
      views.html.error()
    ))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      views.html.error()
    ))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(
      views.html.error()
    ))
  }

}
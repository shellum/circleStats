package utils

import db.{User, UserTableUtils}
import play.api.mvc.{AnyContent, Request}
/**
 * Created by cameron.shellum on 9/7/15.
 */
object ControllerUtil {
  def getEmailFromCookies(request: Request[AnyContent]): Option[String] = {
    val user = getUserFromCookies(request)
    user match {
      case Some(u) => Option(u.email)
      case None => None
    }
  }

  def getUsernameFromCookies(request: Request[AnyContent]): Option[String] = {
    val user = getUserFromCookies(request)
    user match {
      case Some(u) => Option(u.username)
      case None => None
    }
  }

  def getUserFromCookies(request: Request[AnyContent]): Option[User] = {
    val passwordHash = getPasswordHashFromCookies(request)

    UserTableUtils.getUser(passwordHash)
  }

  def getPasswordHashFromCookies(request: Request[AnyContent]): Option[String] = {
    val passwordHash = request.cookies.get("login") match {
      case Some(cookie) => Option(cookie.value)
      case None => None
    }
    passwordHash
  }
}

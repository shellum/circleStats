package db

import java.sql.Date

import slick.driver.PostgresDriver.api._
import utils.Hash

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by cameron.shellum on 8/29/15.
 */
case class User(id: Option[Int] = None, username: String, email: String, passwordHash: String, forgotPasswordHash: Option[String] = None, time: Option[Date] = None)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def * = (id.?, username, email, passwordHash, forgotPasswordHash.?, time.?) <>(User.tupled, User.unapply _) // Must be same order as case class!!!

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def email = column[String]("email")

  def username = column[String]("username")

  def passwordHash = column[String]("password_hash")

  def forgotPasswordHash = column[String]("forgot_password_hash")

  def time = column[Date]("time", O.PrimaryKey, O.AutoInc)
}

object UserTableUtils {
  def addUser(user: User) = {
    val db = Database.forConfig("mydb")
    try {
      val users = TableQuery[UserTable]
      val action = users += user
      val result = db.run(action)

      Await.result(result, 10 seconds)
    } finally db.close
  }

  def updateUser(currentUser: User) = {
    val db = Database.forConfig("mydb")
    try {
      val user = TableQuery[UserTable]
      val action = user.withFilter(_.id === currentUser.id).update(currentUser)
      val result = db.run(action)

      Await.result(result, 10 seconds)
    } finally db.close
  }

  def removeForgotPasswordHash(currentUser: User) = {
    val updatedUser = User(id=currentUser.id,username=currentUser.username,email=currentUser.email,passwordHash=currentUser.passwordHash,None,time=currentUser.time)
    val db = Database.forConfig("mydb")
    try {
      val user = TableQuery[UserTable]
      val action = user.withFilter(_.id === currentUser.id).update(updatedUser)
      val result = db.run(action)

      Await.result(result, 10 seconds)
    } finally db.close
  }

  def addForgotPasswordHash(currentUser: User): String = {
    val forgotPasswordHash = Hash.createHash(24)
    val updatedUser = User(id=currentUser.id,username=currentUser.username,email=currentUser.email,passwordHash=currentUser.passwordHash,forgotPasswordHash=Some(forgotPasswordHash),time=currentUser.time)
    val db = Database.forConfig("mydb")
    try {
      val user = TableQuery[UserTable]
      val action = user.withFilter(_.id === currentUser.id).update(updatedUser)
      val result = db.run(action)

      Await.result(result, 10 seconds)
      forgotPasswordHash
    } finally db.close
  }


  def getUsername(passwordHash: Option[String]): Option[String] = {
    passwordHash match {
      case Some(hash) =>
        val db = Database.forConfig("mydb")
        try {
          val user = TableQuery[UserTable]
          val action = user.withFilter(_.passwordHash === hash).result
          val result = db.run(action)
          val sql = action.statements.head
          val list = Await.result(result, 10 seconds)
          if (list.size == 0)
            None
          else
            Option(list(0).username)
        } finally db.close
      case None => None
    }
  }

  def getPasswordHashFromUsername(username: String): String = {
    val db = Database.forConfig("mydb")
    try {
      val user = TableQuery[UserTable]
      val action = user.withFilter(_.username === username).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      if (list.size == 0)
        ""
      else
        list(0).passwordHash
    } finally db.close

  }

  def getUser(passwordHash: Option[String]): Option[User] = {
    passwordHash match {
      case Some(hash) =>
        val db = Database.forConfig("mydb")
        try {
          val user = TableQuery[UserTable]
          val action = user.withFilter(_.passwordHash === hash).result
          val result = db.run(action)
          val sql = action.statements.head
          val list = Await.result(result, 10 seconds)
          if (list.size == 0)
            None
          else
            Option(list(0))
        } finally db.close
      case None => None
    }
  }
  def getUserFromForgotPasswordHash(forgotPasswordHash: Option[String]): Option[User] = {
    forgotPasswordHash match {
      case Some(hash) =>
        val db = Database.forConfig("mydb")
        try {
          val user = TableQuery[UserTable]
          val action = user.withFilter(_.forgotPasswordHash === forgotPasswordHash).result
          val result = db.run(action)
          val sql = action.statements.head
          val list = Await.result(result, 10 seconds)
          if (list.size == 0)
            None
          else {
            removeForgotPasswordHash(Option(list(0)).get)
            Option(list(0))
          }
        } finally db.close
      case None => None
    }
  }
  def getUserFromEmail(email: String): Option[User] = {
        val db = Database.forConfig("mydb")
        try {
          val user = TableQuery[UserTable]
          val action = user.withFilter(_.email === email).result
          val result = db.run(action)
          val sql = action.statements.head
          val list = Await.result(result, 10 seconds)
          if (list.size == 0)
            None
          else {
            removeForgotPasswordHash(Option(list(0)).get)
            Option(list(0))
          }
        } finally db.close
  }

  def usernameExists(username: String, userId: Option[Int]): Boolean = {
    val db = Database.forConfig("mydb")
    try {
      val user = TableQuery[UserTable]
      var filters = user.withFilter(_.username === username)
      if (userId != None)
        filters = filters.withFilter(_.id =!= userId)
      val action = filters.result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.size != 0

    } finally db.close
  }

}
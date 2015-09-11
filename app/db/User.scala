package db

import java.sql.Date

import slick.driver.PostgresDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by cameron.shellum on 8/29/15.
 */
case class User(id: Option[Int] = None, username: String, email: String, passwordHash: String, time: Option[Date] = None)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def * = (id.?, username, email, passwordHash, time.?) <>(User.tupled, User.unapply _) // Must be same order as case class!!!

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def email = column[String]("email")

  def username = column[String]("username")

  def passwordHash = column[String]("password_hash")

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
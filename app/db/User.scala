package db

import java.sql.Date

import slick.driver.PostgresDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._
/**
 * Created by cameron.shellum on 8/29/15.
 */
case class User(id: Option[Int]=None, email: String, passwordHash: String, time: Option[Date]=None)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def email = column[String]("email")
  def passwordHash = column[String]("password_hash")
  def time = column[Date]("time", O.PrimaryKey, O.AutoInc)

  def * = (id.?, email, passwordHash, time.?) <>(User.tupled, User.unapply _)
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
  def getEmail(passwordHash: Option[String]): Option[String] = {
    passwordHash match {
      case Some(hash) =>
        val db = Database.forConfig("mydb")
        try {
          val user = TableQuery[UserTable]
          val action = user.withFilter (_.passwordHash === hash).result
          val result = db.run (action)
          val sql = action.statements.head
          val list = Await.result (result, 10 seconds)
          if (list.size == 0)
            None
        else
          Option (list (0).email)
        } finally db.close
      case None => None
    }
  }
  def emailExists(email: String): Boolean = {
    val db = Database.forConfig("mydb")
    try {
      val user = TableQuery[UserTable]
      val action = user.withFilter(_.email === email).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.size != 0

    } finally db.close
  }

  def loginOkay(email: String, passwordHash: String): Boolean = {
    val db = Database.forConfig("mydb")
    try {
      val user = TableQuery[UserTable]
      val action = user.withFilter(_.email === email).withFilter(_.passwordHash === passwordHash).result
      val result = db.run(action)
      val sql = action.statements.head
      val list = Await.result(result, 10 seconds)
      list.size != 0

    } finally db.close
  }
}
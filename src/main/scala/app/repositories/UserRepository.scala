package app.repositories

import app.traits.BaseRepos
import app.models.Users.{createUser, getUserByField, serrFormat}
import app.models.{LoginUser, NewUser, User}
import com.github.t3hnar.bcrypt._

abstract class isRegister()
abstract class isAuth()
case class UserCreated() extends isRegister
case class UserAlreadyExist() extends isRegister
case class UserIsAuth() extends isAuth
case class UserNotAuth() extends isAuth

object UserRepository extends BaseRepos {
  def checkPass(loginUser: LoginUser): () => isAuth = {
    getUserByField("login", loginUser.login) match {
      case Some(x) =>
        loginUser.password.isBcrypted(serrFormat(x.toString).password)
        UserIsAuth
      case _ => UserNotAuth
    }
  }

  def createUserFromRegistration(user: NewUser): () => isRegister = {
    val userAuth = getUserByField("login", user.login)

    userAuth match {
      case Some(x) => UserAlreadyExist
      case _ =>
        val userKeys = user.getClass.getDeclaredFields.map(_.getName)
        val result =  for ((elem, idx) <- userKeys.zipWithIndex) yield (elem, user.productElement(idx))
        createUser(result.toVector)
        UserCreated
    }
  }

  def tokenGenerate: String = randomString(30)

  def randomString(len: Int): String = {
    val rand = new scala.util.Random(System.nanoTime)
    val sb = new StringBuilder(len)
    val ab = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    for (i <- 0 until len) {
      sb.append(ab(rand.nextInt(ab.length)))
    }
    sb.toString
  }
}

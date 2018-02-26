package app.repositories

import app.traits.BaseRepos
import app.models.Users.{createUser, getUserByField, serrFormat, serrFormatForUser, tokenUpdate}
import app.models.{LoginUser, User}
import com.github.t3hnar.bcrypt._

abstract class isRegister()
abstract class isAuth()
case class UserCreated() extends isRegister
case class UserAlreadyExist() extends isRegister
case class UserIsAuth() extends isAuth
case class UserIsAuthWithPersonalData(user: String) extends isAuth
case class UserNotAuth() extends isAuth

object UserRepository extends BaseRepos {
  /* Создать новго пользователя */
  def createUserFromRegistration(user: User): () => isRegister = {
    val userAuth = getUserByField("login", user.login)
    userAuth match {
      case Some(x) => UserAlreadyExist
      case _ =>
        val userKeys = user.getClass.getDeclaredFields.map(_.getName)
        val userPair =  for ((elem, idx) <- userKeys.zipWithIndex) yield (elem, user.productElement(idx))
        createUser(userPair.toVector)
        UserCreated
    }
  }

  /* Проверить наличие пользователя и сверить пароль */
  def checkPass(loginUser: LoginUser): () => isAuth = {
    getUserByField("login", loginUser.login) match {
      case Some(x) =>
        val authCheck: Boolean = loginUser.password.isBcrypted(serrFormat(x.toString).password)
        if (authCheck) {
          tokenUpdate("token", loginUser)
          UserIsAuth
        } else UserNotAuth
      case _ => UserNotAuth
    }
  }

  /* Получить пользователя */
  def getPersonalData(login: String, token: String) = {
    getUserByField("login", login) match {
      case Some(user) =>
        val userFormat = serrFormatForUser(user.toString)
        val tokenCheck: Boolean = token == userFormat.token
        if (tokenCheck) {
          UserIsAuthWithPersonalData(user.toString)
        } else UserNotAuth
      case _ => UserNotAuth
    }
  }

  /* Сгенерировать токен */
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

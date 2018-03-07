package app.repositories

import app.traits.BaseRepos
import app.models.Users._
import app.models.{ParamsMap, User}
import com.github.t3hnar.bcrypt._
import app.services.Hashing

case class UserCreated(token: Map[String, String])
case class UserAlreadyExist()
case class UserIsAuth(token: Map[String, String])
case class UserIsAuthWithPersonalData(user: String)
case class UserNotAuth()

object UserRepository extends BaseRepos with Hashing {
  /* Создать новго пользователя */
  def createNewUser(login: String, password: String, user: User) = {
    val userAuth = getUserByField("login", login)
    userAuth match {
      case Some(u) => UserAlreadyExist
      case _ =>
        val tokenG = tokenGenerate(login, "")
        val newUser = user.copy(login = login, password = password.bcrypt, token = Array(tokenG))
        val userKeys = newUser.getClass.getDeclaredFields.map(_.getName)
        val userPair =  for ((elem, idx) <- userKeys.zipWithIndex) yield (elem, newUser.productElement(idx))
        createUser(userPair.toVector)
        UserCreated(tokenG)
    }
  }

  /* Проверить наличие пользователя и сверить пароль и токен */
  def checkPass(login: String, password: String, params: ParamsMap) = {
    getUserByField("login", login) match {
      case Some(u) =>
        val userFromDb = serrFormat(u.toString)
        val authCheck: Boolean = password.isBcrypted(userFromDb.password)
        if (authCheck) {
          val accessId = params.params.getOrElse("accessId", "")
          val tokenFromDb = userFromDb.token.filter(x => x.getOrElse("accessId", "") == accessId)
          if (tokenFromDb.length != 0) {
            val newToken = tokenGenerate(login, accessId);val oldToken = userFromDb.token
            val tokenG = oldToken.map { m =>
              if (m("accessId") == accessId) m.updated("accessToken", newToken("accessToken"))
              else m
            }
            tokenUpdate("token", login, tokenG)
            UserIsAuth(newToken)
          } else {
            val newToken = tokenGenerate(login, accessId);val oldToken = userFromDb.token
            val tokenG = newToken +: oldToken
            tokenUpdate("token", login, tokenG)
            UserIsAuth(newToken)
          }
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
  def tokenGenerate(userLogin: String, accessId: String): Map[String, String] = {
    Map(
      if(accessId != "") "accessId" -> accessId else "accessId" -> md5(userLogin),
      "accessToken" -> randomString(30)
    )
  }
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

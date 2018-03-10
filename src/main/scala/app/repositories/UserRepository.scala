package app.repositories

import app.traits.BaseRepos
import app.models.Users._
import app.models.{ParamsMap, User}
import com.github.t3hnar.bcrypt._
import app.services.Hashing

object UserRepository extends BaseRepos with Hashing {
  case class UserCreated(token: Map[String, String])
  case class UserAlreadyExist()
  case class UserIsAuth(token: Map[String, String])
  case class TokenIsValid()
  case class UserNotAuth()

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
        val uDb = serrFormat(u.toString)
        val authCheck: Boolean = password.isBcrypted(uDb.password)
        if (authCheck) {
          val accessId = params.params.getOrElse("accessId", "")
          val tokenFromDb = uDb.token.filter(x => x.getOrElse("accessId", "") == accessId)
          if (tokenFromDb.length != 0) {
            val newToken = tokenGenerate(login, accessId);val oldToken = uDb.token
            val tokenG = oldToken.map { m =>
              if (m("accessId") == accessId) m.updated("accessToken", newToken("accessToken"))
              else m
            }
            tokenUpdate("token", login, tokenG)
            UserIsAuth(newToken)
          } else {
            val newToken = tokenGenerate(login, accessId);val oldToken = uDb.token
            val tokenG = newToken +: oldToken
            tokenUpdate("token", login, tokenG)
            UserIsAuth(newToken)
          }
        } else UserNotAuth
      case _ => UserNotAuth
    }
  }

  /* Проверить токен */
  def checkToken(login: String, accessId: String, accessToken: String) = {
    getUserByField("login", login) match {
      case Some(u) =>
        val uDb = serrFormatForUser(u.toString)
        val tokenFromDb = uDb.token.filter(x => x.getOrElse("accessId", "") == accessId)
        if (tokenFromDb.length != 0) {
          if (tokenFromDb(0)("accessToken") == accessToken) TokenIsValid
          else UserNotAuth
        } else UserNotAuth
      case _ => UserNotAuth
    }
  }

  /* Получить информацию о пользователе */
  def getPersonalData(p: ParamsMap) = {
      getUserByField("login", p.params.getOrElse("login", "")) match {
      case Some(u) =>
        val uDb = serrFormatForUser(u.toString)
        s""" { "role": "${uDb.role}", "sname": "${uDb.sname}", "phone": "${uDb.phone}", "addresses": "${uDb.addresses}" } """
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

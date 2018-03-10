package app.actors

import akka.actor.{Actor, ActorLogging, Props}
import app.models.{LoginUser, ParamsMap, User}
import app.repositories.UserRepository._

object UserRegistryActor {
  final case class CreateNewUser(login: String, password: String, user: User)
  final case class GetTokenByUser(login: String, password: String, params: ParamsMap)

  def props: Props = Props[UserRegistryActor]
}

class UserRegistryActor extends Actor with ActorLogging {
  import UserRegistryActor._

  var users = Set.empty[User]

  def receive: Receive = {
    case CreateNewUser(login, password, user) =>
      val result = createNewUser(login, password, user)
      sender() ! result
    case GetTokenByUser(login, password, params) =>
      val isAuth = checkPass(login, password, params)
      sender() ! isAuth
  }
}
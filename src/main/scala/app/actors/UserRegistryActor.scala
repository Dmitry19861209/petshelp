package app.actors

import akka.actor.{Actor, ActorLogging, Props}
import app.models.{LoginUser, User}
import app.repositories.UserRepository

object UserRegistryActor {
  final case class CreateNewUser(login: String, password: String, user: User)
  final case class GetTokenByUser(login: String, password: String)

  def props: Props = Props[UserRegistryActor]
}

class UserRegistryActor extends Actor with ActorLogging {
  import UserRegistryActor._

  var users = Set.empty[User]

  def receive: Receive = {
    case CreateNewUser(login, password, user) =>
      val result = UserRepository.createNewUser(login, password, user)
      sender() ! result
    case GetTokenByUser(login, password) =>
      val isAuth = UserRepository.checkPass(login, password)
      sender() ! isAuth
  }
}
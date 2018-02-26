package app.actors

import akka.actor.{Actor, ActorLogging, Props}
import app.models.{LoginUser, User}
import app.repositories.UserRepository

object UserRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetUsers
  final case class CreateUser(user: User)
  final case class GetTokenByUser(loginUser: LoginUser)
  final case class DeleteUser(name: String)

  def props: Props = Props[UserRegistryActor]
}

class UserRegistryActor extends Actor with ActorLogging {
  import UserRegistryActor._

  var users = Set.empty[User]

  def receive: Receive = {
    case CreateUser(user) =>
      val result = UserRepository.createUserFromRegistration(user)
      sender() ! result
    case GetTokenByUser(loginUser) =>
      val isAuth = UserRepository.checkPass(loginUser)
      sender() ! isAuth
    case DeleteUser(name) =>
//      users.find(_.name == name) foreach { user => users -= user }
      sender() ! ActionPerformed(s"User $name deleted.")
  }
}
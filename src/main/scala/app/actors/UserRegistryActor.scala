package app.actors

import akka.actor.{Actor, ActorLogging, Props}
import app.models.{LoginUser, NewUser, User}
import app.repositories.UserRepository

final case class Users(users: Seq[User])

object UserRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetUsers
  final case class CreateUser(user: NewUser)
  final case class GetUser(loginUser: LoginUser)
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
    case GetUser(loginUser) =>
      val isAuth = UserRepository.checkPass(loginUser)
      sender() ! isAuth
    case DeleteUser(name) =>
//      users.find(_.name == name) foreach { user => users -= user }
      sender() ! ActionPerformed(s"User $name deleted.")
  }
}
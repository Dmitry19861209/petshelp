package app.actors

import akka.actor.{Actor, ActorLogging, Props}
import app.models.{LoginUser, User}
import app.repositories.UserRepository

object UserPersonalAccountActor {
  final case class GetUserByToken(login: String, token: String)//TODO Скорее всего такой case class будет использоваться много где.надо вынести

  def props: Props = Props[UserPersonalAccountActor]
}

class UserPersonalAccountActor extends Actor with ActorLogging {
  import UserPersonalAccountActor._

  def receive: Receive = {
    case GetUserByToken(login, token) =>
      val user = UserRepository.getPersonalData(login, token)
      sender() ! user
  }
}
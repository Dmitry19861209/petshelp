package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.headers.BasicHttpCredentials

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import app.actors.UserPersonalAccountActor.GetUserByToken
import app.repositories.{UserIsAuth, UserIsAuthWithPersonalData, UserNotAuth}
import app.traits.RoutesConfig

trait PersonalAccountExpandRoutes extends RoutesConfig {
  implicit def system: ActorSystem
  def userPersonalAccountActor: ActorRef

  def showPersonalData: Route =
    path("get-personal-data") {
      post {
        extractCredentials {
          case Some(BasicHttpCredentials(login, token)) =>
            val getPersonalData = userPersonalAccountActor ? GetUserByToken(login, token)
            onSuccess(getPersonalData) {
              case UserIsAuthWithPersonalData(user) => complete(200 -> s""" { "user" : $user } """)
              case UserNotAuth => complete(401 -> "user not auth")
              case _ => complete(401 -> "")
            }
          case _ => complete(401 -> "")
        }
      }
    }

  def personalAccountRoutes: Route = showPersonalData
}

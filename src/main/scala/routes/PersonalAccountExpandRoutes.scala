package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import app.services.JSON.JsonSupport
import app.traits.RoutesConfig

trait PersonalAccountExpandRoutes extends RoutesConfig {
  def personalRoute(id: Int): Route =
    get {
      complete {
        "Received GET request for personal " + id
      }
    } ~
      put {
        complete {
          "Received PUT request for personal " + id
        }
      }

  def personalAccountRoutes: Route = path("account" / IntNumber) { id => personalRoute(id) }
}

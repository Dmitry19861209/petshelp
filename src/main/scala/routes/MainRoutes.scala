package routes

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.services.JSON.JsonSupport
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

trait MainRoutes extends JsonSupport
  with AuthExpandRoutes
  with PersonalAccountExpandRoutes {

  implicit def system: ActorSystem

//  lazy val log = Logging(system, classOf[MainRoutes])

  lazy val mainRoutes: Route = cors() { authRoutes ~ personalAccountRoutes }

}

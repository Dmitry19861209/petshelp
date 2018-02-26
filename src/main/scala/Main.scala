import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import app.actors.UserRegistryActor
import app.actors.UserPersonalAccountActor
import routes.MainRoutes

object Main extends App with MainRoutes {

  implicit val system: ActorSystem = ActorSystem("MainSystemServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")
  val userPersonalAccountActor: ActorRef = system.actorOf(UserPersonalAccountActor.props, "UserPersonalAccountActor")

  lazy val routes: Route = mainRoutes

  Http().bindAndHandle(routes, "localhost", 8081)

  println(s"Server online at http://localhost:8081")
//
//  Await.result(system.whenTerminated, Duration.Inf)
}

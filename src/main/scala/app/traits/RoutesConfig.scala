package app.traits

import scala.concurrent.duration._
import akka.util.Timeout
import app.services.JSON.JsonSupport
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait RoutesConfig extends JsonSupport {
  implicit lazy val timeout = Timeout(10.seconds) // usually we'd obtain the timeout from the system's configuration
}

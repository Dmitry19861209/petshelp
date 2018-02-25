package app.traits

import scala.concurrent.duration._
import akka.util.Timeout
import app.services.JSON.JsonSupport

trait RoutesConfig extends JsonSupport {
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
}

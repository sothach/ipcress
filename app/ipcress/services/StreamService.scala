package ipcress.services

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import akka.util.Timeout

abstract class StreamService(implicit system: ActorSystem) {
  implicit lazy val executionContext: MessageDispatcher =
    system.dispatchers.lookup(serviceName)
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  protected implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)

  def serviceName: String
}

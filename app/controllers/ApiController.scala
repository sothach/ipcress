package controllers

import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import ipcress.model.{DigestRequest, Format}
import javax.inject.{Inject, Singleton}
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject()(digester: Digester,
                              components: ControllerComponents)
                                          extends AbstractController(components) {
  private implicit val ec: ExecutionContext = components.executionContext

  def digest: Action[Source[Iterator[String], _]] = Action.async(bodySource) { request =>
    val format = Format.fromContentType(request.headers.get("Accepts"))
    digester.execute(request.body.map(list => DigestRequest(list.toSeq,format)))
  }

  private val bodySource: BodyParser[Source[Iterator[String], Any]] = BodyParser { _ =>
    val splitter = Flow[ByteString].map(_.utf8String.split("\n").toIterator)
    Accumulator.source[ByteString].map(src => Right(src via splitter))
  }
}
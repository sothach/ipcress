package controllers

import akka.NotUsed
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

  def digest: Action[Source[Iterator[String], _]] = Action.async(fromFile) { request =>
    val format = request.headers.get("Accepts") match {
      case Some("application/json") =>
        Format.JSON
      case _ =>
        Format.PLAIN
    }
    digester.execute(request.body.map(list => DigestRequest(list.toSeq,format)))
  }

  private val fromFile: BodyParser[Source[Iterator[String], Any]] = BodyParser { _ =>
    val splitter: Flow[ByteString, Iterator[String], NotUsed] =
      Flow[ByteString].map { bytes =>
        bytes.utf8String.split("\n").toIterator
      }

    Accumulator.source[ByteString]
      .map {
        src: Source[ByteString, _] =>
          Right(src via splitter)
      }
  }
}
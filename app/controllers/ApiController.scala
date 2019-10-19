package controllers

import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import ipcress.model.{DigestRequest, Format}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject()(digester: Digester,
                              components: ControllerComponents)
                                          extends AbstractController(components) {
  private implicit val ec: ExecutionContext = components.executionContext
  val logger = Logger(this.getClass)

  def digest: Action[Source[Array[String], _]] = Action.async(fromFile) { request =>
    val format = request.headers.get("Accepts") match {
      case Some("application/json") =>
        Format.JSON
      case _ =>
        Format.PLAIN
    }
    digester.execute(request.body.map(list => DigestRequest(list.toSeq,format)))
  }

  private def fromFile: BodyParser[Source[Array[String], _]] = BodyParser { _ =>
    def splitter = Flow[ByteString].map(_.utf8String.lines.toArray)
    val result: Accumulator[ByteString, Right[Nothing, Source[Array[String], Any]]] =
      Accumulator.source[ByteString].map(_.via(splitter)).map { src: Source[Array[String],Any] =>
        Right[Nothing,Source[Array[String],Any]](src)
      }
    result
  }

}

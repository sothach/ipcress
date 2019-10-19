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

  def digest: Action[Source[Array[String], _]] = Action.async(fromFile) { request =>
    val format = request.headers.get("Accepts") match {
      case Some("application/json") =>
        Format.JSON
      case _ =>
        Format.PLAIN
    }
    digester.execute(request.body.map(list => DigestRequest(list.toSeq,format)))
  }

  private val fromFile: BodyParser[Source[Array[String], Any]] = BodyParser { _ =>
    val splitter = Flow[ByteString].map(_.utf8String.lines.toArray)
    val result: Accumulator[ByteString, Either[Result, Source[Array[String], Any]]] =
      Accumulator.source[ByteString].map(_.via(splitter)).map(Right.apply)
    result
  }
  
  /*
  found   : Source[Array[Object],Any] => Right[Nothing,Source[Array[Object],Any]]
  required: Source[Array[Object],Any] => Either[play.api.mvc.Result,Source[Array[String],Any]]
   */
}
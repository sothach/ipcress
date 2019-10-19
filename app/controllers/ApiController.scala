package controllers

import akka.NotUsed
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
    val requestSource = request.body.map(list => DigestRequest(list.toSeq,format))
    digester.execute(requestSource)
  }

  /*
  [error]  found   : Source[Array[Object],Any] => Right[Nothing,Source[Array[Object],Any]]
  [error]  required: Source[Array[Object],Any] => Either[Result,Source[Array[String], _]]
   */
  private val fromFile: BodyParser[Source[Array[String], Any]] = BodyParser { request =>
    def splitter = Flow[ByteString].map(_.utf8String.lines.toArray)
    val result: Accumulator[ByteString, Right[Nothing, Source[Array[String], Any]]] =
      Accumulator.source[ByteString].map(_.via(splitter)).map(Right.apply)
    result
  }

}

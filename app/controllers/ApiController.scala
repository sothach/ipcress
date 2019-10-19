package controllers

import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import ipcress.model.{DigestRequest, Format}
import ipcress.services.DigesterService
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.util.{Failure, Success}

@Singleton
class ApiController @Inject()(digesterService: DigesterService,
                              components: ControllerComponents)
                                          extends AbstractController(components) {
  import digesterService._
  val logger = Logger(this.getClass)

  def digest: Action[Source[Array[String], _]] = Action.async(fromFile) { request =>
    val format = request.headers.get("Accepts") match {
      case Some("application/json") =>
        Format.JSON
      case _ =>
        Format.PLAIN
    }
    val requestSource = request.body.map(list => DigestRequest(list.toSeq,format))
    digestFromSource(requestSource) map {
      case Success(result) => Ok(result)
      case Failure(t) =>
        InternalServerError(t.getMessage)
    }
  }

  private def fromFile: BodyParser[Source[Array[String], _]] = BodyParser { request =>
    def splitter = Flow[ByteString].map(_.utf8String.lines.toArray)
    Accumulator.source[ByteString].map(_.via(splitter)).map(Right.apply)
  }

}

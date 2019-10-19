package controllers

import akka.stream.scaladsl.Source
import ipcress.model.DigestRequest
import ipcress.services.DigesterService
import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

protected class Digester @Inject() (digesterService: DigesterService) {
  import digesterService._

  def execute(source: Source[DigestRequest, _]): Future[Result] = {
    val response: Seq[Try[String]] => Seq[Result] = (results: Seq[Try[String]]) => results map {
      case Success(result) =>
        Ok(result)
      case Failure(t) =>
        InternalServerError(t.getMessage)
    }
    (digestFromSource(source) map response)
      .map(_.headOption.getOrElse(InternalServerError))
  }
}

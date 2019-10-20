package ipcress.services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import ipcress.model._
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class DigesterService @Inject()(implicit system: ActorSystem) extends StreamService {
  val serviceName: String = "digester-service"

  def digestFromSource(request: Source[DigestRequest,_]): Future[Seq[Try[String]]] =
    request.async via filter via digester via formatter runWith Sink.seq

  private val filter: Flow[DigestRequest, DigestRequest, NotUsed] = Flow[DigestRequest]
    .collect { case element: DigestRequest if element.ipNumbers.nonEmpty => element }

  private val digester: Flow[DigestRequest, Try[DigestRequest], NotUsed] =
    Flow[DigestRequest] map(request =>
      Try(IpDigester(request.ipNumbers)).map(digest => request.copy(result = digest)))

  private val formatter: Flow[Try[DigestRequest], Try[String], NotUsed] =
    Flow[Try[DigestRequest]].map(_.map(Formatter.format))

}
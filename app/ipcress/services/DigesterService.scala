package ipcress.services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import ipcress.model.Format.Format
import ipcress.model._
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class DigesterService @Inject()(implicit system: ActorSystem) extends StreamService {
  val serviceName: String = "digester-dispatcher"

  private case class Frame(groups: Map[IPv4, Seq[Series]], format: Format)
  private object Frame {
    def apply(values: (Map[IPv4, Seq[Series]],Format)): Frame = Frame(values._1, values._2)
  }

  def digestFromSource(request: Source[DigestRequest,_]): Future[Seq[Try[String]]] =
    request.async via filter via digester via formatter runWith Sink.seq

  private val filter: Flow[DigestRequest, DigestRequest, NotUsed] = Flow[DigestRequest]
    .collect { case element @ DigestRequest(ips, _) if ips.nonEmpty => element }

  private val digester: Flow[DigestRequest, Try[Frame], NotUsed] =
    Flow[DigestRequest] map(request =>
      Try((IpDigester(request.ipNumbers),request.format)).map(Frame.apply))

  private val formatter: Flow[Try[Frame], Try[String], NotUsed] =
    Flow[Try[Frame]].map(_.map(frame => Formatter.format(frame.format,frame.groups)))
}
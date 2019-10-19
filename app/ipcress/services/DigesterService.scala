package ipcress.services

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import ipcress.model.Format.Format
import ipcress.model._
import javax.inject.{Inject, Singleton}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class DigesterService @Inject()(implicit val system: ActorSystem) {
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("digester-dispatcher")
  //implicit val ec: ExecutionContextExecutor = system.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  logger.info("DigesterService: starting")

  private case class Frame(groups: Map[IPv4, Seq[Series]], format: Format)
  private object Frame {
    def apply(values: (Map[IPv4, Seq[Series]],Format)): Frame = Frame(values._1, values._2)
  }

  def digestFromSource(request: Source[DigestRequest,_]): Future[Try[String]] =
    request.async via filter via digest via transform runWith Sink.head

  private val filter: Flow[DigestRequest, DigestRequest, NotUsed] = Flow[DigestRequest]
    .collect { case element @ DigestRequest(ips, _) if ips.nonEmpty => element }

  private val digest: Flow[DigestRequest, Try[Frame], NotUsed] =
    Flow[DigestRequest] map(request =>
      Try((IpDigester(request.ipNumbers),request.format)).map(Frame.apply))

  private val transform: Flow[Try[Frame], Try[String], NotUsed] =
    Flow[Try[Frame]].map(_.map(frame => Formatter.format(frame.format,frame.groups)))
}
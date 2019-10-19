import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import ipcress.model.{DigestRequest, Format}
import ipcress.services.DigesterService
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class DigesterServiceSpec extends WordSpecLike with MockitoSugar with MustMatchers {
  private implicit val system: ActorSystem = ActorSystem.create("test-actor-system")

  "The DigesterService" should {
    val subject = new DigesterService
    "start-up correctly" in {
      val request = Source.single(DigestRequest(ipNumbers,Format.JSON))
      val results = Await.result(subject.digestFromSource(request), Duration.Inf)
      results match {
        case Success(map) =>
          Json.parse(map) mustBe expectedJson
        case Failure(t) =>
          fail(t)
      }
    }
  }

  "The DigesterService" should {
    val subject = new DigesterService

    "catch processing errors" in {
      val results = Await.result(subject.digestFromSource(
        Source.single(DigestRequest(Seq("aa.123.123.123")))), Duration.Inf)
      results match {
        case Success(_) =>
          fail("call expected to fail")
        case Failure(t) =>
          t.getMessage mustBe ("""For input string: "aa"""")
      }
    }
  }

  private val ipNumbers: Seq[String] = Seq(
    "99.243.62.20", "99.243.64.40", "99.243.64.40", "99.243.64.41", "99.243.64.42",
    "99.244.106.35", "99.244.121.59", "99.244.121.60", "99.244.121.61", "99.244.156.149",
    "99.244.178.5", "99.244.198.185", "99.244.84.79", "99.245.110.185", "99.245.157.208",
    "99.245.157.209", "99.245.157.210", "99.245.157.220", "99.245.157.221", "99.245.157.230",
    "99.245.157.231", "99.245.157.240", "99.245.159.212", "99.245.168.213", "99.245.55.191",
    "99.245.86.16")

  private val expectedJson = Json.parse("""[
      |{"ip":["99.243.62.20"]},
      |{"ip":["99.243.64.40","99.243.64.42"]},
      |{"ip":["99.244.84.79"]},
      |{"ip":["99.244.106.35"]},
      |{"ip":["99.244.121.59","99.244.121.61"]},
      |{"ip":["99.244.156.149"]},{"ip":["99.244.178.5"]},
      |{"ip":["99.244.198.185"]},{"ip":["99.245.55.191"]},
      |{"ip":["99.245.86.16"]},
      |{"ip":["99.245.110.185"]},
      |{"ip":["99.245.157.208","99.245.157.210"]},
      |{"ip":["99.245.157.220","99.245.157.221"]},
      |{"ip":["99.245.157.230","99.245.157.231"]},
      |{"ip":["99.245.157.240"]},
      |{"ip":["99.245.159.212"]},
      |{"ip":["99.245.168.213"]}]""".stripMargin)
}


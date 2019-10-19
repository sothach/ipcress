package routes

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class RouteSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {
  private val system = ActorSystem.create("test-actor-system")
  private implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system))(system)

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .loadConfig(Configuration(ConfigFactory.load("application.conf")))
    .build()

  "POST /digest with Accept=text/plain" should {
    "digest the request data and return a simple listt" in {
      val request = FakeRequest("POST", "/digest")
        .withHeaders(FakeHeaders(Map("Host" -> "localhost", "Accepts" -> "text/plain").toSeq))
        .withBody(testData)

      route(app, request) foreach { future =>
        val response = Await.result(future, 10 seconds)
        response.header.status mustBe OK
        val result = Await.result(response.body.consumeData, 2 seconds)

        result.utf8String mustBe expectPlain
      }
    }
  }

  private val expectPlain = """99.243.62.20
                              |99.243.64.40-99.243.64.42
                              |99.244.84.79
                              |99.244.106.35
                              |99.244.121.59-99.244.121.61
                              |99.244.156.149
                              |99.244.178.5
                              |99.244.198.185
                              |99.245.55.191
                              |99.245.86.16
                              |99.245.110.185
                              |99.245.157.208-99.245.157.210
                              |99.245.157.220-99.245.157.221
                              |99.245.157.230-99.245.157.231
                              |99.245.157.240
                              |99.245.159.212
                              |99.245.168.213""".stripMargin

  private val expectJson = Json.parse("""[
                             |  {"ip": ["99.243.62.20"]},
                             |  {"ip": ["99.243.64.40", "99.243.64.42"]},
                             |  {"ip": ["99.244.84.79"]},
                             |  {"ip": ["99.244.106.35"]},
                             |  {"ip": ["99.244.121.59", "99.244.121.61"]},
                             |  {"ip": ["99.244.156.149"]},
                             |  {"ip": ["99.244.178.5"]},
                             |  {"ip": ["99.244.198.185"]},
                             |  {"ip": ["99.245.55.191"]},
                             |  {"ip": ["99.245.86.16"]},
                             |  {"ip": ["99.245.110.185"]},
                             |  {"ip": ["99.245.157.208", "99.245.157.210"]},
                             |  {"ip": ["99.245.157.220", "99.245.157.221"]},
                             |  {"ip": ["99.245.157.230", "99.245.157.231"]},
                             |  {"ip": ["99.245.157.240"]},
                             |  {"ip": ["99.245.159.212"]},
                             |  {"ip": ["99.245.168.213"]}
                             |]""".stripMargin)

  private val testData =
             """99.243.62.20
               |99.243.64.40
               |99.243.64.40
               |99.243.64.41
               |99.243.64.42
               |99.244.106.35
               |99.244.121.59
               |99.244.121.60
               |99.244.121.61
               |99.244.156.149
               |99.244.178.5
               |99.244.198.185
               |99.244.84.79
               |99.245.110.185
               |99.245.157.208
               |99.245.157.209
               |99.245.157.210
               |99.245.157.220
               |99.245.157.221
               |99.245.157.230
               |99.245.157.231
               |99.245.157.240
               |99.245.159.212
               |99.245.168.213
               |99.245.55.191
               |99.245.86.16""".stripMargin
}
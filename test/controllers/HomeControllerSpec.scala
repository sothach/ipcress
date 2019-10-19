package controllers

import akka.actor.ActorSystem
import ipcress.model.DigestRequest
import ipcress.services.DigesterService
import org.mockito.Mockito.when
import org.scalatest.MustMatchers
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.mvc.{Request, Result, Results}
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class HomeControllerSpec extends PlaySpec with Results with MockitoSugar with MustMatchers {
  private implicit val system: ActorSystem = ActorSystem.create("test-actor-system")
  private implicit val ec: ExecutionContext = system.dispatcher
  private val digesterService = new DigesterService

  "The HomeController" should {
    val subject = new HomeController(digesterService, stubControllerComponents())
    "return InternalServerError in" in {
      val request: Request[DigestRequest] = mock[Request[DigestRequest]]
      when(request.body).thenReturn(DigestRequest( "256.123.123.123","plain"))
      val result: Future[Result] = subject.process(request)
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "requirement failed: 256 should be 0 to 255"
      subject.dataForm match {
        case Form(mapping,fields,errors,data) =>
          println(s"$mapping $fields $errors $data")
      }
    }
  }

}


package controllers

import akka.actor.ActorSystem
import ipcress.model.DigestRequest
import ipcress.services.DigesterService
import org.mockito.Mockito.when
import org.scalatest.MustMatchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{DefaultMessagesApi, Messages}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.helper.form

import scala.concurrent.{ExecutionContext, Future}

class HomeControllerSpec extends PlaySpec with Results with MockitoSugar with MustMatchers {
  private implicit val system: ActorSystem = ActorSystem.create("test-actor-system")
  private implicit val ec: ExecutionContext = system.dispatcher
  private val digester = new Digester(new DigesterService)

  "The HomeController" should {
    val subject = new HomeController(digester, stubControllerComponents())
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

  "Form" should {
    val subject = new HomeController(digester, stubControllerComponents())
    "be valid" in {
      val messagesApi = new DefaultMessagesApi(
        Map("en" ->Map("error.min" -> "minimum")))
      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = {
        FakeRequest("POST", "/").withFormUrlEncodedBody("ipNumbers" -> "123.124.125.126", "format" -> "json")
      }
      implicit val messages: Messages = messagesApi.preferred(request)

      def errorFunc(badForm: Form[DigestRequest]) = {
        BadRequest(badForm.errorsAsJson)
      }
      def successFunc(userData: DigestRequest) = {
        Ok("""{"ipNumbers":["123.124.125.126"],"format":"json"}""")
      }
      val result = Future.successful(subject.dataForm.bindFromRequest().fold(errorFunc, successFunc))
      val form = subject.dataForm.fill(DigestRequest("123.123.123.123","json"))
      val requestData = form.value
      println(s"form: $form => $requestData")
      println(contentAsString(result))
      //Json.parse(contentAsString(result)) must be(Json.obj("format" -> JsString("json")))
    }
  }

}


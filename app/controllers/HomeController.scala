package controllers

import akka.stream.scaladsl.Source
import ipcress.model.DigestRequest
import ipcress.services.DigesterService
import javax.inject._
import org.slf4j.LoggerFactory
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.util.{Failure, Success}

@Singleton
class HomeController @Inject()(digesterService: DigesterService, cc: ControllerComponents)
          extends AbstractController(cc) with I18nSupport {
  import digesterService._
  private val logger = LoggerFactory.getLogger(this.getClass)

  val dataForm = Form(
    mapping("ipNumbers" -> nonEmptyText, "format" -> nonEmptyText
    )(DigestRequest.apply)(DigestRequest.unapply)
  )
  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.landing(dataForm))
  }

  def process: Action[DigestRequest] = Action.async(parse.form(dataForm)) { implicit request =>
    logger.debug(s"request received: ${request.body}")
    val requestSource = Source.single(DigestRequest(request.body.ipNumbers,request.body.format))
    digestFromSource(requestSource) map {
      case Success(result) => Ok(result)
      case Failure(t) =>
        InternalServerError(t.getMessage)
    }
  }

}

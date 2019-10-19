package controllers

import akka.stream.scaladsl.Source
import ipcress.model.DigestRequest
import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(digester: Digester, cc: ControllerComponents)
          extends AbstractController(cc) with I18nSupport {
  private implicit val ec: ExecutionContext = cc.executionContext

  val dataForm = Form(
    mapping("ipNumbers" -> nonEmptyText, "format" -> nonEmptyText
    )(DigestRequest.apply)(DigestRequest.unapply)
  )
  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.landing(dataForm))
  }

  def process: Action[DigestRequest] = Action.async(parse.form(dataForm)) { implicit request =>
    digester.execute(
      Source.single(DigestRequest(request.body.ipNumbers,request.body.format)))
  }

}

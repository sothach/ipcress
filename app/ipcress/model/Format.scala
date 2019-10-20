package ipcress.model

object Format extends Enumeration {
  type Format = Value
  val JSON, PLAIN = Value
  def fromContentType(cType: Option[String]): Format = cType match {
    case Some(play.api.http.MimeTypes.JSON) =>
      Format.JSON
    case _ =>
      Format.PLAIN
  }
}

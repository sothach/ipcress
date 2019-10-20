package ipcress.model

import ipcress.model.Format.Format

case class DigestRequest(ipNumbers: Seq[String],
                         format: Format = Format.PLAIN,
                         result: Map[IPv4, Seq[Series]] = Map.empty)
object DigestRequest {
  def apply(data: String, format: String): DigestRequest =
    DigestRequest(data.split("\n"), format)
  def apply(data: Seq[String], format: String): DigestRequest = {
    val ftype = format.toLowerCase.trim match {
      case "json" =>
        Format.JSON
      case _ =>
        Format.PLAIN
    }
    DigestRequest(data,ftype)
  }
  def unapply(request: DigestRequest): Option[(String, String)] =
    Some(request.ipNumbers.mkString("\n"), request.format.toString.toLowerCase)
}

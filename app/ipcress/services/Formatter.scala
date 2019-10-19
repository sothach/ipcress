package ipcress.services

import ipcress.model.Format.Format
import ipcress.model.{Format, IPv4, Series}
import play.api.libs.json.{JsString, Json}

trait Formatter {
  protected def renderSingle(base: IPv4, series: Series): String
  protected def renderRange(base: IPv4, series: Series): String
  protected def serialize(results: Seq[String]): String

  private def render(base: IPv4, series: Series): String = if (series.isSingular) {
    renderSingle(base, series)
  } else {
    renderRange(base, series)
  }

  def format[T](ipGroups: Map[IPv4, scala.Seq[Series]]): String = {
    val result = ipGroups.toSeq.sortBy(_._1) flatMap { case (base, items) =>
      items map (item => render(base, item))
    }
    serialize(result)
  }
}
object Formatter {
  def format(format: Format, groups: Map[IPv4, scala.Seq[Series]]): String =
    (format match {
      case Format.JSON => new JsonFormatter
      case Format.PLAIN => new PlainFormatter
    }).format(groups)
}

class PlainFormatter extends Formatter {
  protected def renderSingle(base: IPv4, series: Series): String =
    s"${base(4) = series.min}"
  protected def renderRange(base: IPv4, series: Series): String =
    s"${base(4) = series.min}-${base(4) = series.max}"
  protected def serialize(results: Seq[String]): String = results.mkString("\n")
}

class JsonFormatter extends Formatter {
  private def asJson(values: Seq[IPv4]) =
    Json.stringify(Json.obj("ip" -> values.map(ip => JsString(ip.toString))))
  protected def renderSingle(base: IPv4, series: Series): String =
    asJson(Seq(base(4) = series.min))
  protected def renderRange(base: IPv4, series: Series): String =
    asJson(Seq(base(4) = series.min, base(4) = series.max))
  protected def serialize(results: Seq[String]): String = "["+results.mkString(",")+"]"
}

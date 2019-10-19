package ipcress.model

import scala.annotation.tailrec

case class Series(min: Int, max: Int) {
  val isSingular: Boolean = min == max
}

object Series {
  def apply(value: Int): Series = Series(value, value)

  @tailrec
  def rangify(items: Seq[Int], last: Option[Int] = None,
              acc: Seq[Series] = Seq.empty): Seq[Series] =
    items.headOption match {
      case Some(head) =>
        rangify(items.drop(1), Some(head), last match {
          case None => Seq(Series(head))
          case Some(l) if head == l + 1 =>
            acc.lastOption.map(_.copy(max = head)) match {
              case Some(updated) =>
                acc.dropRight(1) :+ updated
              case None =>
                acc
            }
          case Some(_) => acc :+ Series(head)
        })
      case None =>
        acc
    }
}
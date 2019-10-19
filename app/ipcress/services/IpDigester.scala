package ipcress.services

import ipcress.model.{IPv4, Series}

object IpDigester {

  def apply(ipList: Seq[String]): Map[IPv4, Seq[Series]] = ipList
    .distinct
    .map(IPv4(_))
    .sorted
    .groupBy(_.value >> 8)
    .map { case (head, values) =>
      (IPv4((head << 8) + values.head(4)),
        Series.rangify(values.map(_ (4))))
    }

}

package model

import ipcress.model.{DigestRequest, Format}
import org.scalatest.{MustMatchers, WordSpecLike}

class ModelSpec extends WordSpecLike with MustMatchers {

  "A DigestRequest" should {
    "be created from text values" in {
      val subject = DigestRequest("127.0.0.1\n127.1.1.0", "json")
      subject.format must be(Format.JSON)
      subject.ipNumbers must be(Seq("127.0.0.1", "127.1.1.0"))
    }
    "default to type PLAIN" in {
      val subject = DigestRequest(Seq("127.0.0.1", "127.1.1.0"), "plain")
      DigestRequest.unapply(subject) equals Some("127.0.0.1\n127.1.1.0", "plain")
    }
  }

}


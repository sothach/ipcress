package model

import ipcress.model.{DigestRequest, Format, IPv4}
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

  "An IPv4 object" should {
    val subject = IPv4("121.122.123.124")
    "be copied and updated" in {
      val updated = subject(4) = 121
      updated.toString must be("121.122.123.121")
    }
    "return an octet value" in {
      subject(2) must be(122)
    }
    "guard against invalid queries" in {
      val caught = intercept[IllegalArgumentException] {
        subject(5)
      }
      caught.getMessage must be("requirement failed: octet to query must be between 1 and 4")
    }
    "guard against invalid updates" in {
      val caught = intercept[IllegalArgumentException] {
        subject(5) = 121
      }
      caught.getMessage must be("requirement failed: octet to assign must be between 1 and 4")
    }
  }

}


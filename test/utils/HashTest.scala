package utils
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

/**
 * Created by cameron.shellum on 8/26/15.
 */
class HashTest extends Specification {

  "createHash" should {
    "be able to create hashes of length 16" in {
      Hash.createHash(16) must have size (16)
    }
  }

  "createHash" should {
    "only return hashes with lowercase alpha characters" in {
      val hash = Hash.createHash(16)
      val pattern = "[a-z]{16}".r
      pattern.findAllIn(hash).hasNext must beEqualTo(true)
    }
  }
}
package utils

/**
 * Created by cameron.shellum on 8/26/15.
 */
object Hash {

  def createHash(len: Int): String = {
    var str = ""
    for (i <- 1 to len) {
      str = str + ((Math.random() * 1000 % 26) + 97).toChar
    }
    str
  }

}

package app.services

import java.security.MessageDigest
import org.joda.time.DateTime
import java.time.Instant


trait Hashing {
  def md5(s: String): String = {
//    val date = DateTime.now()
    val unixTimestamp : Long = Instant.now.getEpochSecond
    val sd = s + Instant.now.getEpochSecond
    val m = java.security.MessageDigest.getInstance("MD5")
    val b = sd.getBytes("UTF-8")
    m.update(b, 0, b.length)
    new java.math.BigInteger(1, m.digest()).toString(10)
  }
}

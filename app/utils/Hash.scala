package utils

import java.net.URLEncoder
import java.text.SimpleDateFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.Base64
import play.api.Play
import play.api.Play.current
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global

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

  def sendContactInfo(email: String, message: String) = {
    // TODO: Pull email from env
    sendEmail("","Contact Message",message)
  }

  def createSignature(email: String, forgotPasswordHash: String, secureSite: Boolean, host: String) = {
    val siteUrl = secureSite match {
      case true => "https://" + host
      case false => "http://" + host
    }
    val message = "<html><body>Hi,<br>Someone requested a password reset.</h1>Go <a href='" + siteUrl + "/forgotPassword/" + forgotPasswordHash + "'>here</a> to reset yout password.<br><br>Thanks,<br>CircleStats</body></html>"

    sendEmail(email,"CircleStats Password Reset Request", message)
  }

  def sendEmail(to: String, subject: String, message: String) = {
    val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    val clearText = format.format(new java.util.Date())
    val awsSecret = Play.configuration.getString("aws-secret").get

    val hMac = Mac.getInstance("HmacSHA256")
    hMac.init(new SecretKeySpec(awsSecret.getBytes("UTF-8"), "HmacSHA256"))
    val signature = new String(Base64.encodeBase64(hMac.doFinal(clearText.getBytes("UTF-8"))))

    val headerXAmznAuthorization = "AWS3-HTTPS AWSAccessKeyId=" + Play.configuration.getString("aws-key").get + ", Algorithm=HmacSHA256, Signature=" + signature

    val emailAddress = new String(URLEncoder.encode(to).getBytes("UTF-8"))
    // TODO: Pull to email from env
    val fromAddress = new String(URLEncoder.encode("").getBytes("UTF-8"))

    val encodedMessage = new String(URLEncoder.encode(message).getBytes("UTF-8"))

    val body = "Action=SendEmail" +
      "&Content=text/html" +
      "&Destination.ToAddresses.member.1=" + emailAddress +
      "&Message.Body.Html.Data=" + encodedMessage +
      "&Message.Subject.Data=" + subject +
      "&Source=" + fromAddress

    // TODO: pull mail server from env
    val future = WS.url("")
      .withHeaders(("Content-Type", "application/x-www-form-urlencoded"), ("Date", clearText), ("X-Amzn-Authorization", headerXAmznAuthorization))
      .post(body).map { response =>
            println(response.body)
            println(response.status)
            println(response.statusText)
    }
  }

}

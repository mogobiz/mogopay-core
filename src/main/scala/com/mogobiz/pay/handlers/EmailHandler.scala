package com.mogobiz.pay.handlers

import com.mogobiz.pay.config.Settings

/**
 * From https://gist.github.com/mariussoutier/3436111
 */
object EmailHandler {

  sealed abstract class MailType

  case object Plain extends MailType

  case object Rich extends MailType

  case object MultiPart extends MailType

  case class Mail(from: (String, String), // (email -> name)
                  to: Seq[String],
                  cc: Seq[String] = Seq.empty,
                  bcc: Seq[String] = Seq.empty,
                  subject: String,
                  message: String,
                  richMessage: Option[String] = None,
                  attachment: Option[(java.io.File)] = None)

  object Send {
    def to(mail: Mail) {
      import org.apache.commons.mail._

      val format =
        if (mail.attachment.isDefined) MultiPart
        else if (mail.richMessage.isDefined) Rich
        else Plain

      val commonsMail: Email = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
        case MultiPart => {
          val attachment = new EmailAttachment()
          attachment.setPath(mail.attachment.get.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(mail.attachment.get.getName)
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
        }
      }

      commonsMail.setHostName(Settings.Mail.Smtp.Host)
      commonsMail.setSmtpPort(Settings.Mail.Smtp.Port)
      commonsMail.setSslSmtpPort(Settings.Mail.Smtp.SslPort.toString)
      if (Settings.Mail.Smtp.Username.length > 0) {
        commonsMail.setAuthenticator(new DefaultAuthenticator(
          Settings.Mail.Smtp.Username,
          Settings.Mail.Smtp.Password))
      }
      commonsMail.setSSLOnConnect(Settings.Mail.Smtp.IsSSLEnabled)
      commonsMail.setSSLCheckServerIdentity(Settings.Mail.Smtp.IsSSLCheckServerIdentity)
      commonsMail.setStartTLSEnabled(Settings.Mail.Smtp.IsStartTLSEnabled)

      // Can't add these via fluent API because it produces exceptions
      mail.to foreach (commonsMail.addTo(_))
      mail.cc foreach (commonsMail.addCc(_))
      mail.bcc foreach (commonsMail.addBcc(_))

      commonsMail.
        setFrom(mail.from._1, mail.from._2).
        setSubject(mail.subject).
        send()
    }
  }

}

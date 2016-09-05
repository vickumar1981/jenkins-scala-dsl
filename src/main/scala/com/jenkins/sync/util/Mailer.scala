package com.jenkins.sync.util

import java.util.Properties
import javax.mail.{Message, MessagingException, PasswordAuthentication, Session, Transport}
import javax.mail.internet.{InternetAddress, MimeMessage}

import com.typesafe.scalalogging.LazyLogging

object Mailer extends LazyLogging {
  object smtp extends JenkinsConfigProvider {
    lazy val auth = configSettings.getBoolean("mail.smtp.auth").toString
    lazy val enableTLS = configSettings.getBoolean("mail.smtp.starttls.enable").toString
    lazy val host = configSettings.getString("mail.smtp.host")
    lazy val port = configSettings.getInt("mail.smtp.port").toString
    lazy val user = configSettings.getString("mail.smtp.user")
    lazy val password = configSettings.getString("mail.smtp.password")
    lazy val from = configSettings.getString("mail.smtp.from")
    lazy val contentType = "text/html; charset=utf-8"
  }

  private def emailProperties = {
    val props = new Properties()
    props.put("mail.smtp.auth", smtp.auth)
    props.put("mail.smtp.starttls.enable", smtp.enableTLS)
    props.put("mail.smtp.host", smtp.host)
    props.put("mail.smtp.port", smtp.port)
    props
  }

  private def emailSession () = {
    val props = emailProperties
    val session = if (smtp.auth == "true")
      Session.getInstance(props,
        new javax.mail.Authenticator() {
          protected override def getPasswordAuthentication(): PasswordAuthentication =
            new PasswordAuthentication(smtp.user, smtp.password)
        })
      else Session.getInstance(props)
    session
  }


  private def emailMessage (s: Session, to:String, subject: String, text:String) = {
    val message = new MimeMessage(s)
    message.setFrom(new InternetAddress(smtp.from))
    message.setRecipients(Message.RecipientType.TO, to.toLowerCase)
    message.setSubject(subject)
    message.setContent(text, smtp.contentType)
    message
  }

  def sendEmail (to: String, subject: String, text: String) = {
    val session = emailSession
    try {
      val message = emailMessage(session, to, subject, text)
      Transport.send(message)
    } catch {
      case (e: MessagingException) =>
        logger.error("Error sending email to '%s': %s".format(to, e.toString))
        e.printStackTrace()
    }
  }

}
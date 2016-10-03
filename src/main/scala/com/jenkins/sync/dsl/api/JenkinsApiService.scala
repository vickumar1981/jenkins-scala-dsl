package com.jenkins.sync.dsl.api

import com.jenkins.sync.model.GitOrgRepo
import com.jenkins.sync.util.{SerializeJson, JenkinsConfigProvider}
import com.typesafe.config.{ConfigObject, ConfigValue, ConfigFactory, Config}
import java.util.Map.Entry

import dispatch.Defaults._
import dispatch._

import collection.JavaConversions._
import scala.collection.JavaConverters._

import com.typesafe.scalalogging.LazyLogging

abstract class JenkinsApiService extends LazyLogging {
  object config extends JenkinsConfigProvider {
    lazy val user = configSettings.getString("jenkins.credentials.user")
    lazy val token = configSettings.getString("jenkins.credentials.token")
    lazy val baseUrl = configSettings.getString("jenkins.url")
    lazy val gitUrl = configSettings.getString("github.url")
    lazy val protocols: List[String] = List("http://", "https://")
    lazy val organizations = listOrganizations()
    lazy val organizationsWhiteList =
      configSettings.getStringList("github.organizations.whitelist").toList

    lazy val userEmails: Map[String, String] = {
      val list : Iterable[ConfigObject] = configSettings.getObjectList("github.emails").toList
      (for {
        item : ConfigObject <- list
        entry : Entry[String, ConfigValue] <- item.entrySet().asScala
        key = entry.getKey
        email = entry.getValue.unwrapped().toString
      } yield (key, email)).toMap
    }

    private def listOrganizations(): List[String] = {
      val req = url(urls.listGitOrgs).GET
      api.handleRequest[List[String]](req, "Unable to fetch git organizations from url.",
        (content) => {
          SerializeJson.read[List[GitOrgRepo]](content) match {
            case (string, Some(repos)) => repos.map(_.login.toLowerCase)
            case (string, None) => {
              logger.error("Couldn't serialize git repos.")
              List.empty
            }
          }
        }, (msg) => {
          logger.error(msg)
          List.empty
        }, (err) => {
          logger.error(err)
          List.empty
        }, isJson=true)
    }
  }

  object api {
    def handleRequest[T](req: Req, msg: String,
                         successCallback: (String) => T,
                         redirectCallback: (String) => T,
                         errorCallback: (String) => T,
                         isJson: Boolean = false) = {
      try {
        val resp = (if (isJson) Http(req OK as.String) else Http(req OK as.xml.Elem)).either()
        resp match {
          case Right(content) => successCallback(content.toString)
          case Left(StatusCode(302)) => redirectCallback(msg)
          case Left(e: Throwable) => errorCallback(e.getMessage)
          case _ => errorCallback("Unknown exception.")
        }
      }
      catch {
        case t: Throwable => errorCallback(t.getMessage)
      }
    }

    def authRequest(reqUrl: String): Req = url(reqUrl).as_!(config.user, config.token)
    def hasAuth(): Boolean = config.user.nonEmpty && config.token.nonEmpty
    def get(reqUrl: String): Req =
      if (hasAuth) authRequest(reqUrl).GET else url(reqUrl).GET
    def post(reqUrl: String): Req =
      if (hasAuth) authRequest(reqUrl).POST else url(reqUrl).POST
  }

  object urls {
    def gitOrg(url: String) = url.split("/").tail.headOption.getOrElse("")
    def verify(urlInput: String) =
      (if (urlInput.endsWith("/")) urlInput.dropRight(1) else urlInput).replaceAll(" ", "%20")
    def isWhiteListed(gitUrl: String): Boolean =
      config.organizationsWhiteList.contains(gitOrg(gitUrl))
    def stripProtocol(url: String) = {
      var newUrl: String = url
      config.protocols.foreach { proto => newUrl = newUrl.replaceAll(proto, "") }
      newUrl
    }

    def wipeWsUrl(jobUrl: String) = "%s/doWipeOutWorkspace".format(verify(jobUrl))
    def jobConfigUrl(jobUrl: String) = "%s/config.xml".format(verify(jobUrl))
    def listJobsUrl(): String = "%s/api/json".format(verify(config.baseUrl))
    def listGitOrgs(): String = "%s/api/v3/organizations?per_page=100".format(verify(config.gitUrl))
    def listPlugins(): String = "%s/pluginManager/api/json?depth=1".format(verify(config.baseUrl))
  }
}

package com.jenkins.sync.dsl

import com.jenkins.sync.dsl.jenkins._
import com.jenkins.sync.model.{JobResult, Job}

class JenkinsJob (val jobData: Job) {
  object workspace {
    def clean(): JobResult = {
      val wipeWsUrl = urls.wipeWsUrl(jobData.url)
      val req = jenkins.api.post(wipeWsUrl)
      jenkins.api.handleRequest[JobResult](req, "Workspace cleaned",
        (content) => JobResult(jobData, true, List("Workspace cleaned")),
        (msg) => JobResult(jobData, true, List(msg)),
        (err) => JobResult(jobData, false, List(err)))
    }
  }

  object xml {
    private lazy val jobConfigRequest = jenkins.api.get(urls.jobConfigUrl(jobData.url))
    private lazy val xmlData: Option[scala.xml.Elem] = fetchXmlData()

    private def fetchXmlData() = jenkins.api.handleRequest[Option[scala.xml.Elem]](
        jobConfigRequest, "No job configuration",
        (content) => Some(scala.xml.XML.loadString(content)),
        (msg) => None, (err) => None)

    def apply(): Option[scala.xml.Elem] = xmlData

    def checkGitUrls(): JobResult = if (xmlData.isDefined) {
        val badUrls = jenkins.checkGitUrls(xmlData.toString)
        if (badUrls.isEmpty)
          JobResult(jobData, true, List("No personal repos"))
        else
          JobResult(jobData, false, badUrls)
      }
      else JobResult(jobData, true, List("No personal repos"))
  }
}

object JobConversions {
  implicit def JobToJenkinsJob(in: Job) = new JenkinsJob(in)
}

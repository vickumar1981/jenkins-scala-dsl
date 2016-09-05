package com.jenkins.sync.dsl

import com.jenkins.sync.dsl.api.JenkinsApiService
import com.jenkins.sync.dsl.templates.EmailTemplates
import com.jenkins.sync.model.{JobResult, Jobs, Job}
import com.jenkins.sync.util.{Mailer, SerializeJson}

import com.typesafe.scalalogging.LazyLogging

object jenkins extends JenkinsApiService with LazyLogging {
  def checkGitUrls(jobXml: String): List[String] = {
    val badUrls = scala.collection.mutable.ListBuffer[String]()
    jobXml.split{">"}.map(_.replaceAll("\n", "").trim.toLowerCase).map { u =>
      val newUrl = urls.verify(u.split("<").headOption.getOrElse("")).replaceAll("\\.git", "")
      val gitUrl = urls.stripProtocol(newUrl)
      if (gitUrl.startsWith(urls.stripProtocol(urls.verify(config.gitUrl))) &&
        !config.organizations.contains(urls.gitOrg(gitUrl)) &&
        !urls.isWhiteListed(gitUrl)) {
        if (!badUrls.contains(newUrl))
          badUrls += newUrl
      }
      else if (gitUrl.startsWith("github.com") &&
        (!urls.isWhiteListed(gitUrl))) {
        if (!badUrls.contains(newUrl))
          badUrls += newUrl
      }
    }
    badUrls.toList
  }

  object jobs {
    private def listJobs(jobPattern: Option[String] = None) = {
      val req = api.get(urls.listJobsUrl)
      api.handleRequest[List[Job]](req, "Unable to reach jenkins jobs url.",
        (content) => SerializeJson.read[Jobs](content) match {
          case (string, Some(jobs)) =>
            jobs.jobs.filter(j => (jobPattern.isEmpty || j.name.matches(jobPattern.get)))
          case (string, None) => {
            logger.error("Couldn't serialize jobs.")
            List.empty
          }},
        (msg) => {
          logger.error("Couldn't serialize jobs, redirection error.")
          List.empty
        },
        (err) => {
          logger.error("Couldn't serialize jobs: %s.".format(err))
          List.empty
        }, isJson=true)
    }

    def apply(): List[Job] = listJobs()
    def apply(jobPattern: String) = listJobs(Some(jobPattern))

    def run(func: (Job) => JobResult, jobPattern: Option[String] = None,
            filterWhen: (JobResult) => Boolean = (JobResult) => true,
            processResults: Option[List[JobResult] => Any] = None) = {
      val results = scala.collection.mutable.ListBuffer[JobResult]()
      var succeeded = 0
      var failed = 0
      val jobsList = if (jobPattern.isDefined) jobs(jobPattern.get) else jobs()

      for (j <- jobsList) {
        val jobResult = func(j)
        if (jobResult.success)
          succeeded += 1
        else
          failed +=1
        if (filterWhen(jobResult)) {
          results += jobResult
          logger.info("%s: %s".format(j.name, jobResult.values.mkString("[", ", ", "]")))
        }
      }
      logger.info("total succeeded: %s, total failed: %s\n".format(succeeded, failed))
      if (processResults.isDefined) {
        val doProcess = processResults.get
        doProcess(results.toList)
      }
      results.toList
    }
  }

  object users {
    def sendInvalidReposEmails(results: List[JobResult]) = {
      logger.info("Emailing users...")
      val reposList: List[(String, String, String)] =
        results.filter(!_.success)
          .map(r => r.values.map(v => (urls.gitOrg(urls.stripProtocol(v)), r.job.name, v))).flatten
      val users = reposList.map(_._1).distinct.sortWith(_ < _)
      users.filter(u => config.userEmails.exists(_._1 == u)).foreach(u => {
        val userEmail = config.userEmails(u)
        val userRepos = reposList.filter(r => r._1 == u).sortWith(_._2 < _._2)
        val emailTemplate = EmailTemplates.invalidPrivateReposTemplate(
          urls.verify(config.baseUrl), userEmail, userRepos)
        Mailer.sendEmail(userEmail, "Private repos reminder", emailTemplate)
        logger.info("Sent email to '%s (%s)'".format(userEmail, u))
      })
      val usersWithNoEmails = users.filterNot(u => config.userEmails.exists(_._1 == u))
      logger.warn("No emails found for: %s".format(usersWithNoEmails.mkString(", ")))
      results
    }
  }
}



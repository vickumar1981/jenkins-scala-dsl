package com.jenkins.sync.dsl.templates

object EmailTemplates {

  def invalidPersonalReposTemplate(jenkinsUrl: String, user: String,
                                  repos: List[(String, String, String)]) = {
    """
      |<h3>Personal repositories for %s</h3>
      |<p>Hi %s:
      |<br/><br/>
      |You are receiving this notificaiton because the following jenkins jobs are using
      |personal git repositories:
      |</p>
      |<table>
      | <tr>
      |   <td><b><u>Job</u></b></td>
      |   <td><b><u>Repository</u></b></td>
      | </tr>
      | %s
      |</table>
    """.format(jenkinsUrl, user, repos.map {
          r => "<tr><td><a href='%s/job/%s'>%s</a></td> <td>%s</td></tr>"
            .format(jenkinsUrl, r._2, r._2, r._3)}.mkString("\n"))
      .stripMargin
  }
}

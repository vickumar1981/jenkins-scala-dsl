package com.jenkins.sync.dsl.templates

object EmailTemplates {

  def invalidPersonalReposTemplate(jenkinsUrl: String, user: String,
    repos: List[(String, String, String)]) = {
    """
      |<h3>Personal repositories for %s</h3>
      |<p>Hi %s:
      |<br/><br/>
      |You are receiving this notification because the following jenkins jobs are using
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

  def noVirtualEnvRequired(jenkinsUrl: String, repos: List[(String, String)]) = {
    """
      |<h3>Jobs that do not need a virtualenv for %s</h3>
      |<br/>
      |<p>
      |Virtualenv’s are a useful tool for installing python packages.
      |However, our jenkins environments come with a few python packages pre-installed!
      |If your job installs ansible or fabric, you may no longer need to manage the
      |virtualenv/pip install steps yourself.</p>
      |
      |<p>You are receiving this notification because we believe that the following jenkins jobs
      |do not require a virtualenv setup because its packages are redundant.</p>
      |
      |<p>Note that removing the virtualenv/pip install steps are <u><i>completely optional</i></u>, but we think
      |your code will be easier to understand, and it will execute a lot faster when running
      |with a cleared workspace.</p>
      |
      |<table>
      | <tr>
      |   <td><b><u>Job</u></b></td>
      |   <td><b><u>Package</u></b></td>
      | </tr>
      | %s
      |</table>
    """.format(jenkinsUrl, repos.map {
      r => "<tr><td><a href='%s/job/%s'>%s</a></td> <td>%s</td></tr>"
        .format(jenkinsUrl, r._1, r._1, r._2)}.mkString("\n"))
      .stripMargin
  }
}

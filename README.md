# jenkins-scala-dsl #

A Scala DSL wrapper for the Jenkins REST Api.

### What can I do? ###

* Quickly iterate over jenkins jobs and get information or execute a task
```
import com.jenkins.sync.dsl.jenkins
import com.jenkins.sync.dsl.JobConversions._
import com.jenkins.sync.util._

object Main {
  def main(args: Array[String]) {
    JenkinsHelperConfig.loadConfig("testConfig.conf")
    jenkins.jobs().foreach { j => println(j.name) }
  }
}
```
* Apply a function to each Job
```
import com.jenkins.sync.dsl.jenkins
import com.jenkins.sync.dsl.JobConversions._
import com.jenkins.sync.util._

object Main {
  def main(args: Array[String]) {
    JenkinsHelperConfig.loadConfig("testConfig.conf")
    for (j <- jenkins.jobs()) {
        // Currently, the only two things you can do with a job
        j.workspace.clean()
        j.xml.checkGitUrls()
    }
  }
}
```

### How do I get set up? ###

* Add a `project/Build.scala` which contains the following:
```
import sbt._
import Keys._

object MyBuild extends Build {
  lazy val myProject = Project(
    id="my-project",
    base=file(".")) settings(
    name := "my-project",
    mainClass:= Some("Main")
    ) dependsOn(jenkinsDslProject)

  lazy val jenkinsDslProject =
    RootProject(uri("git://github.com/vickumar1981/jenkins-scala-dsl.git"))

}
```
* Add a configuration file like the sample below (This is the file you load with the JenkinsHelperConfig):
```
jenkins {
  credentials {
    user = ""
    token = ""
  }
  url = "http://localhost:8081"
}

github {
  url = ""
  organizations {
    whitelist = [ ]
  }
  emails = [ ]
}

mail {
  smtp {
    auth = true
    host = "smtp.gmail.com"
    port = 587
    user = ""
    from = ""
    password = ""
    starttls {
      enable = true
    }
  }
}
```
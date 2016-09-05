package com.jenkins.sync.model

case class Label()
case class Jobs(_class: String, assignedLabels: Option[List[Label]],
                nodeDescription: String, nodeName: String, numExecutors: Int,
                description: String, jobs: List[Job])
case class Job(_class: String, name: String, url: String, color: String)
case class JobResult(job: Job, success: Boolean, values: List[String] = List.empty)

case class GitOrgRepo(login: String, id: Int, url: String, repos_url: String,
                      events_url: String, hooks_url: String, issues_url: String, members_url: String,
                      public_members_url: String, avatar_url: String, description: String)
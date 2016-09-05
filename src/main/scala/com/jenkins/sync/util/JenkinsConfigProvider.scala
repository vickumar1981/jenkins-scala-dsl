package com.jenkins.sync.util

trait JenkinsConfigProvider {
  def configSettings = JenkinsHelperConfig.config
}

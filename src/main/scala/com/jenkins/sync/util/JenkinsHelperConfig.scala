package com.jenkins.sync.util

import java.io.File

import com.typesafe.config.{ConfigFactory, Config}
import java.util.concurrent.atomic.AtomicReference

object JenkinsHelperConfig {
  private val configHolder = new AtomicReference[Config](ConfigFactory.empty())

  def config = configHolder.get()

  def replace(newConfig: Config) {
    configHolder.set(newConfig)
  }

  def loadConfig(configFilePath: String): Config = {
    val configFile = new File(configFilePath)
    require(configFile.exists(), s"The argument provided '${configFilePath}' refers to a file that doesn't exist.")

    val config = ConfigFactory.parseFile(configFile)
    verifyConfiguration(config)

    JenkinsHelperConfig.replace(config)
    config
  }

  private def verifyConfiguration(config: Config) {
    verify(config).containsString("jenkins.url")
    verify(config).containsString("jenkins.credentials.user")
    verify(config).containsString("jenkins.credentials.token")
    verify(config).containsString("github.url")
    verify(config).containsStringList("github.organizations.whitelist")
  }

  private def verify(config: Config) = new {
    def containsInt(key: String) {
      require(doesNotThrow {
        config.getInt(key)
      }, s"Config at `$key` not found or not an Int")
    }

    def containsString(key: String) {
      require(doesNotThrow {
        config.getString(key)
      }, s"Config at `$key` not found or not a String")
    }

    def containsStringList(key: String) {
      require(doesNotThrow {
        config.getStringList(key)
      }, s"Config at `$key` not found or not a String List")
    }
  }

  private def doesNotThrow(thunk: => Any): Boolean = {
    try {
      val _ = thunk
      true
    } catch {
      case x: Throwable => throw x
    }
  }
}
package com.jenkins.sync.util

import java.nio.charset.StandardCharsets

import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonParser
import net.liftweb.json.compact
import net.liftweb.json.pretty
import net.liftweb.json.render

object SerializeJson {
  object RenderType extends Enumeration {
    val Compact = Value
    val Pretty = Value
  }

  import RenderType.{Compact, Pretty}

  private implicit val formats = DefaultFormats
  private var renderType: RenderType.Value = Pretty

  def setRenderType(value: RenderType.Value) {
    renderType = value
  }

  def read[T : Manifest](bytes: Array[Byte]): (Array[Byte], String, Option[T]) = {
    val raw = new String(bytes, StandardCharsets.UTF_8)
    (bytes, raw, Option(fromStringToJson[T](raw)))
  }

  def read[T : Manifest](raw: String): (String, Option[T]) = {
    (raw, Option(fromStringToJson[T](raw)))
  }

  def fromStringToJson[T : Manifest](raw: String): T = {
    JsonParser.parse(raw).extract[T]
  }

  def fromStreamToJson[T : Manifest](raw: java.io.Reader) = {
    JsonParser.parse(raw).extract[T]
  }

  def write(data: Any): String = {
    renderType match {
      case Compact => compact(render(decompose(data)))
      case Pretty => pretty(render(decompose(data)))
    }
  }

  def writeCompact(data: Any): String = {
    compact(render(decompose(data)))
  }
}
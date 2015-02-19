package com.mogobiz.pay.handlers

import java.io.{File, InputStreamReader}
import javax.script.{ScriptEngineFactory, Invocable, ScriptEngine, ScriptEngineManager}

import com.mogobiz.pay.model.Mogopay.Account
import com.mogobiz.pay.settings.Settings
import com.mogobiz.template.Mustache

class TemplateHandler {
  def mustache(template: String, jsonString: String): (String, String) = {
    val mailContent = Mustache(template, jsonString)
    val eol = mailContent.indexOf('\n')
    require(eol > 0, "No new line found in mustache file to distinguish subject from body")
    val subject = mailContent.substring(0, eol)
    val body = mailContent.substring(eol + 1)
    (subject, body)

  }


  def loadTemplateByVendor(vendor: Option[Account], templateName: String) : String = {

    def defaultTemplate() = scala.io.Source.fromInputStream(classOf[TemplateHandler].getResourceAsStream("/template/" + templateName)).mkString

    vendor.map { v =>
      v.company.map {
        c => new File(new File(Settings.TemplatesPath, c), templateName)
      } getOrElse (new File(new File(Settings.TemplatesPath), templateName))
    } map { templateFile =>
      if (templateFile.exists()) {
        val source = scala.io.Source.fromFile(templateFile)
        val lines = source.mkString
        source.close()
        lines
      }
      else defaultTemplate()
    } getOrElse(defaultTemplate())
  }
}


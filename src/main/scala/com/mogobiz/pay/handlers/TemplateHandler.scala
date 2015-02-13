package com.mogobiz.pay.handlers

import java.io.{File, InputStreamReader}
import javax.script.{ScriptEngineFactory, Invocable, ScriptEngine, ScriptEngineManager}

import com.mogobiz.pay.model.Mogopay.Account
import com.mogobiz.pay.settings.Settings
import com.mogobiz.template.Mustache

class TemplateHandler {
  def mustache(template: String, jsonString: String): String = {
    Mustache(template, jsonString)
  }

  def loadTemplateByVendor(vendor: Option[Account], templateName: String) : String = {
    val templateFile = if (vendor.isDefined) Some(new File(new File(Settings.TemplatesPath, vendor.get.company.get), templateName)) else None
    if (templateFile.isDefined && templateFile.get.exists()) {
      val source = scala.io.Source.fromFile(templateFile.get)
      val lines = source.mkString
      source.close()
      lines
    }
    else {
      scala.io.Source.fromInputStream(classOf[TemplateHandler].getResourceAsStream("/template/" + templateName)).mkString
    }
  }

}


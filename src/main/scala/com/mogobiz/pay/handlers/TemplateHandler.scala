package com.mogobiz.pay.handlers

import java.io.{File, InputStreamReader}
import java.util.Locale
import javax.script.{ScriptEngineFactory, Invocable, ScriptEngine, ScriptEngineManager}

import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Mogopay.Account
import com.mogobiz.template.Mustache
import org.apache.commons.lang.LocaleUtils

class TemplateHandler {
  def mustache(template: String, jsonString: String): (String, String) = {
    val mailContent = Mustache(template, jsonString)
    val eol = mailContent.indexOf('\n')
    require(eol > 0, "No new line found in mustache file to distinguish subject from body")
    val subject = mailContent.substring(0, eol)
    val body = mailContent.substring(eol + 1)
    (subject, body)

  }


  def loadTemplateByVendor(vendor: Option[Account], templateName: String, locale: Option[String]) : String = {
    def findExternalTemplate(company: Option[String], templateName: String) = {
      val file = company.map { c =>
        new File(new File(Settings.TemplatesPath, c), s"$templateName.mustache")
      } getOrElse (new File(new File(Settings.TemplatesPath), s"$templateName.mustache"))
      if (file.exists()) {
        val source = scala.io.Source.fromFile(file)
        val lines = source.mkString
        source.close()
        Some(lines)
      }
      else None
    }

    def defaultTemplate() = scala.io.Source.fromInputStream(classOf[TemplateHandler].getResourceAsStream(s"/template/$templateName.mustache")).mkString

    vendor.map { v =>
      locale.map { l =>
        findExternalTemplate(v.company, s"${templateName}_$l") getOrElse {
          findExternalTemplate(v.company, s"${templateName}_${LocaleUtils.toLocale(l).getDisplayLanguage}") getOrElse {
            findExternalTemplate(v.company, templateName) getOrElse {
              defaultTemplate()
            }
          }
        }
      } getOrElse {
        findExternalTemplate(v.company, templateName) getOrElse {
          defaultTemplate()
        }
      }
    } getOrElse {
      defaultTemplate()
    }
  }
}


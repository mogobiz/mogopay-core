/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers

import java.io.{FileInputStream, File, InputStream}
import java.util.Locale
import javax.script.{ScriptEngineFactory, Invocable, ScriptEngine, ScriptEngineManager}

import com.mogobiz.pay.config.Settings
import com.mogobiz.pay.model.Account
import com.mogobiz.template.Mustache
import org.apache.commons.lang.LocaleUtils

class TemplateHandler {
  def mustache(vendor: Option[Account],
               templateName: String,
               locale: Option[String],
               jsonString: String): (String, String) = {
    val customJS    = loadCustomJsByVendor(vendor, locale)
    val template    = loadTemplateByVendor(vendor, templateName, locale)
    val mailContent = Mustache(template, customJS, jsonString)
    val eol         = mailContent.indexOf('\n')
    require(eol > 0, "No new line found in mustache file to distinguish subject from body. Subject cannot be empty.")
    val subject = mailContent.substring(0, eol)
    val body    = mailContent.substring(eol + 1)
    (subject, body)

  }

  private def loadCustomJsByVendor(vendor: Option[Account], locale: Option[String]): Option[InputStream] = {
    val standardName = "custom"
    val prefixe      = "js"
    val file = vendor.map { v =>
      findExternalFileForCompanyAndLanguage(standardName, prefixe, v.company, locale)
    }.getOrElse {
      findExternalFileForCompanyAndLanguage(standardName, prefixe, None, locale)
    }

    file.map { f =>
      new FileInputStream(f)
    }
  }

  private def loadTemplateByVendor(vendor: Option[Account], templateName: String, locale: Option[String]): String = {
    val prefixe = "mustache"
    val file = vendor.map { v =>
      findExternalFileForCompanyAndLanguage(templateName, prefixe, v.company, locale)
    }.getOrElse {
      findExternalFileForCompanyAndLanguage(templateName, prefixe, None, locale)
    }

    file.map { f =>
      val source = scala.io.Source.fromFile(f)
      val lines  = source.mkString
      source.close()
      lines
    }.getOrElse {
      scala.io.Source
        .fromInputStream(classOf[TemplateHandler].getResourceAsStream(s"/template/$templateName.mustache"))
        .mkString
    }
  }
  private def findExternalFileForCompanyAndLanguage(standardName: String,
                                                    prefixe: String,
                                                    company: Option[String],
                                                    locale: Option[String]): Option[File] = {
    // On cherche en fonction de la langue dans le répertoire de la compagnie
    val localeFile = locale.flatMap { l =>
      loadExistingExternalFile(company, s"${standardName}_$l.$prefixe").map { f =>
        Some(f)
      }.getOrElse {
        loadExistingExternalFile(company, s"${standardName}_${LocaleUtils.toLocale(l).getLanguage}.$prefixe").map {
          f =>
            Some(f)
        }.getOrElse {
          None
        }
      }
    }

    localeFile.map { f =>
      Some(f)
    }.getOrElse {
      loadExistingExternalFile(company, s"$standardName.$prefixe")
    }
  }

  private def loadExistingExternalFile(company: Option[String], fileName: String): Option[File] = {
    val companyFile = company.flatMap { c =>
      loadExistingExternalFileFromParent(new File(Settings.TemplatesPath, c), fileName)
    }

    companyFile.map { f =>
      Some(f)
    }.getOrElse {
      loadExistingExternalFileFromParent(new File(Settings.TemplatesPath), fileName)
    }
  }

  private def loadExistingExternalFileFromParent(parent: File, fileName: String): Option[File] = {
    val file = new File(parent, fileName)
    if (file.exists()) Some(file)
    else None
  }

}

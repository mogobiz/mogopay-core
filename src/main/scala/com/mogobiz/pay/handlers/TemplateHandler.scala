package com.mogobiz.pay.handlers

import java.io.InputStreamReader
import javax.script.{ScriptEngineFactory, Invocable, ScriptEngine, ScriptEngineManager}

import com.mogobiz.template.Mustache

class TemplateHandler {
  def mustache(template: String, jsonString: String): String = {
    Mustache(template, jsonString)
  }
}


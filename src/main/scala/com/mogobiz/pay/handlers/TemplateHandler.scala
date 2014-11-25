package com.mogobiz.pay.handlers

import java.io.InputStreamReader
import javax.script.{ScriptEngineFactory, Invocable, ScriptEngine, ScriptEngineManager}

class TemplateHandler {
  def mustache(template: String, jsonString: String): String = {
    val manager: ScriptEngineManager = new ScriptEngineManager
    val engineManager: ScriptEngineManager = new ScriptEngineManager
    val engine: ScriptEngine = engineManager.getEngineByName("rhino")
    engine.eval(new InputStreamReader(classOf[TemplateHandler].getResourceAsStream("/template/mustache.js")))
    val invocable: Invocable = engine.asInstanceOf[Invocable]
//    val template: String = "Email addresses of {{contact.name}}:\n" + "{{#contact.emails}}\n" + "- {{.}}\n" + "{{/contact.emails}}"
//
//    val json: String = "{" + "\"contact\": {" + "\"name\": \"Mr A\", \"emails\": [" + "\"contact@some.tld\", \"sales@some.tld\"" + "]}}"

    val json: AnyRef = engine.eval("JSON")
    val data: AnyRef = invocable.invokeMethod(json, "parse", jsonString)
    val mustache: AnyRef = engine.eval("Mustache")
    invocable.invokeMethod(mustache, "render", template, data).toString
  }
}

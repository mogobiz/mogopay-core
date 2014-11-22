package com.mogobiz.pay.handlers

import java.io.InputStreamReader
import javax.script.{Invocable, ScriptEngine, ScriptEngineManager}

class TemplateHandler {
  def applyMustache(template: String, json: String) {
    val manager: ScriptEngineManager = new ScriptEngineManager
    val engineManager: ScriptEngineManager = new ScriptEngineManager
    val engine: ScriptEngine = engineManager.getEngineByName("rhino")
    engine.eval(new InputStreamReader(classOf[TemplateHandler].getResourceAsStream("/template/mustache.js")))
    val invocable: Invocable = engine.asInstanceOf[Invocable]
    val template: String = "Email addresses of {{contact.name}}:\n" + "{{#contact.emails}}\n" + "- {{.}}\n" + "{{/contact.emails}}"

    val contactJson: String = "{" + "\"contact\": {" + "\"name\": \"Mr A\", \"emails\": [" + "\"contact@some.tld\", \"sales@some.tld\"" + "]}}"

    val json: AnyRef = engine.eval("JSON")
    val data: AnyRef = invocable.invokeMethod(json, "parse", contactJson)
    val mustache: AnyRef = engine.eval("Mustache")
    System.out.println(invocable.invokeMethod(mustache, "render", template, data))
  }
}

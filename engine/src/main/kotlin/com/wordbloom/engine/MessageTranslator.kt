package com.wordbloom.engine

/** Translates reviewed display templates; puzzle answers and identifiers are never mutated. */
class MessageTranslator(entries: List<Pair<String,String>>) {
 private data class Template(val pattern: Regex,val slots: List<String>,val target: String)
 private val exact=entries.filter { !Regex("\\{\\d+}").containsMatchIn(it.first) }.toMap()
 private val templates=entries.filter { Regex("\\{\\d+}").containsMatchIn(it.first) }.sortedByDescending { it.first.replace(Regex("\\{\\d+}"),"").length }.map { (source,target) ->
  val slots=mutableListOf<String>();val pattern=StringBuilder("^");var offset=0
  Regex("\\{\\d+}").findAll(source).forEach { slot ->
   pattern.append(Regex.escape(source.substring(offset,slot.range.first))).append("(.*?)")
   slots+=slot.value;offset=slot.range.last+1
  }
  pattern.append(Regex.escape(source.substring(offset))).append('$')
  Template(Regex(pattern.toString(),RegexOption.DOT_MATCHES_ALL),slots,target)
 }
 fun text(source: String): String = translate(source,0)
 private fun translate(source: String,depth: Int): String {
  exact[source]?.let { return it }
  if(depth>2)return source
  templates.forEach { template ->
   val match=template.pattern.matchEntire(source)?:return@forEach
   val values=template.slots.mapIndexed { index,key -> key to translate(match.groupValues[index+1],depth+1) }.toMap()
   // Replace once, so a user's text or a price containing braces stays literal.
   return Regex("\\{\\d+}").replace(template.target) { values[it.value]?:it.value }
  }
  return source
 }
}

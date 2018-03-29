package app.services.JSON

import app.models.User

trait ParserStruct {
  def formResponseData(uDb: User) = {
    val userKeys = uDb.getClass.getDeclaredFields.map(_.getName)
    val userPairFilter =  userKeys.zipWithIndex filter { case (elem, idx) => elem != "login" && elem != "password" && elem != "token" }
    val userPair = userPairFilter map { case (elem, idx) =>
        val prEl = uDb.productElement(idx)
        if(uDb.productElement(idx).isInstanceOf[String]) "\"" + elem + "\": \"" + prEl + "\""
        else "\"" + elem + "\": " + convertS(prEl)
    }
    s"""{ ${userPair.mkString(",")} }"""
  }

  def convertS(struct: Any): String = struct match {
    case elems: Seq[String] =>
      "[" + (elems map convertS mkString ", ") + "]"
    case bindings: Map[String, String] =>
      val assocs = bindings map { case (key, value) => "\"" + key + "\":" + convertS(value) }
      "{" + (assocs mkString ", ") + "}"
    case arr: Array[Map[String, String]] =>
      "[" + (arr map convertS mkString ", ") + "]"
    case str: String => '\"' + str + '\"'
    case _ => "\"\""
  }
}

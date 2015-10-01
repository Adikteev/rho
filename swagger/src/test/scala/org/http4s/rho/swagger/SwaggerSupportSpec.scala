package org.http4s
package rho
package swagger

import org.specs2.mutable.Specification

import org.http4s.rho.bits.MethodAliases.GET

class SwaggerSupportSpec extends Specification with RequestRunner {

  override def transforms: RouteMiddleWare = SwaggerSupport().middleware

  import org.json4s.JsonAST._
  import org.json4s.jackson._

  lazy val service = new RhoService {
    GET / "hello" |>> { () => Ok("hello world") }
    GET / "hello"/ pathVar[String] |>> { world: String => Ok("hello " + world) }
  }

  "SwaggerSupport" should {

    "Expose an API listing" in {
      val r = Request(GET, Uri(path = "/swagger.json"))

      val JObject(List((a, JObject(_)), (b, JObject(_)), (c, JObject(_)))) =
        parseJson(checkOk(r)) \\ "paths"

      Set(a, b, c) should_== Set("/swagger.json", "/hello", "/hello/{string}")
    }
  }
}

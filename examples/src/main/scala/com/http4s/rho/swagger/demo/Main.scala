package com.http4s.rho.swagger.demo

import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.server.Service
import org.http4s.server.blaze.BlazeBuilder

object Main extends App {
  val middleware = SwaggerSupport().middleware
  BlazeBuilder
    .mountService(Service.withFallback(StaticContentService.routes)(MyService.toService(middleware)))
    .bindLocal(8080)
    .start
    .run
    .awaitShutdown()
}

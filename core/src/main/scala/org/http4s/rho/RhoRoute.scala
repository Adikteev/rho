package org.http4s
package rho

import bits.HeaderAST.HeaderRule
import bits.PathAST.PathRule
import org.http4s.rho.bits.QueryAST.QueryRule
import org.http4s.rho.bits.ResultInfo

import shapeless.HList

import scalaz.concurrent.Task

/** A shortcut type to bundle everything needed to define a route */
final case class RhoRoute[T <: HList](router: RoutingEntity[T], action: Action[T]) {

  def apply(req: Request, hlist: T): Task[Response] = action.act(req, hlist)

  def method: Method = router.method
  def path: PathRule = router.path
  def query: QueryRule = router.query
  def headers: HeaderRule = router.headers
  def responseEncodings: Set[MediaType] = action.responseEncodings
  def resultInfo: Set[ResultInfo] = action.resultInfo
  def validMedia: Set[MediaRange] = router match {
    case r: CodecRouter[_,_] => r.decoder.consumes
    case _ => Set.empty
  }
}

object RhoRoute {
  type Tpe = RhoRoute[_ <: HList]
}

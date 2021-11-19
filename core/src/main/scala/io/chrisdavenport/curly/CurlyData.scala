package io.chrisdavenport.curly

import cats._
import cats.syntax.all._

case class CurlyData(
  method: String,
  uri: String,
  headers: List[(String, String)],
  binaryBody: Option[String],
  originalOpts: List[CurlyParser.Opts]
)

object CurlyData {
  def fromOpts[F[_]: MonadThrow](opts: List[CurlyParser.Opts]): F[CurlyData] = {
    import CurlyParser.Opts._
    val method = opts.collectFirstSome{
      case Opt("request", value) => value.some
      case _ => None 
    }.getOrElse("GET")
    val uri = opts.collectFirstSome{
      case Unhandled(value) => value.pure[F].some
      case _ => None
    }.getOrElse(new Throwable("No Uri Component Found").raiseError)
    val headers = opts.collect{
      // Hackity Hack
      case Opt(header, value) if (value.split(":").toList.size == 2) => value.split(":").toList match {
        case header :: value :: Nil => 
          (header.trim(), value.trim()).pure[F]
        case _ => new Throwable(s"Incorrect Header $header $value").raiseError[F, (String, String)]
      }
    }.sequence

    val body = opts.collectFirst{
      case Opt("data-binary", value) => value
    }

    for {
      u <- uri
      h <- headers
    } yield CurlyData(
      method,
      u,
      h,
      body,
      opts
    )
  }
}
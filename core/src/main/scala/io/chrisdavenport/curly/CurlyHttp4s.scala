package io.chrisdavenport.curly

import cats._
import org.http4s._
import fs2.Pure
import CurlyParser._
import cats.syntax.all._

object CurlyHttp4s {

  def fromData[F[_]: MonadThrow](data: CurlyData): F[(Request[Pure], String)] = {
    val method = Method.fromString(data.method)
    val uri = Uri.fromString(data.uri)
    val body = data.binaryBody

    for {
      m <- method.liftTo[F]
      u <- uri.liftTo[F]
    } yield {
      val base = Request[Pure](
        Method.fromString(m.toString()).fold(throw _, identity),
        Uri.unsafeFromString(data.uri),
        headers = Headers(data.headers.map(t => Header.ToRaw.keyValuesToRaw(t)))
      )
      val outReq = body.fold(base){body => 
        val newBody = fs2.Stream(body).through(fs2.text.encode(java.nio.charset.StandardCharsets.UTF_8))
        base.withBodyStream(newBody)
      }
      val bodyText = body.fold("Stream()")(s => s"""Stream("$s").through(text.encode(StandardCharsets.UTF_8))""")
      val string = s"""{
      |  import _root_.org.http4s.{Request, Method, Uri, Headers}
      |  import _root_.fs2.{Stream, Pure${if(body.isDefined) ", text" else ""}}${if(body.isDefined) "\n  import _root_.java.nio.charset.StandardCharsets" else ""}
      |  Request[Pure](
      |    method = Method.fromString("${m.toString}").fold(throw _, identity),
      |    uri = Uri.unsafeFromString("${data.uri}"),
      |    headers = Headers(${data.headers.map(printTuple2).intercalate(",")}),
      |    body = $bodyText
      |  )
      |}""".stripMargin
      (outReq, string)
    }
  }

  def printTuple2(t: (String, String)): String = "(\"" + t._1 + "\",\"" + t._2 + "\")"
}
package io.chrisdavenport.curly

import cats.effect._
import cats.syntax.all._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    // val x = "curl 'https://linux.die.net/man/1/curl' -H 'User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:94.0) Gecko/20100101 Firefox/94.0' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8' -H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'Referer: https://www.google.com/' -H 'Connection: keep-alive' -H 'Cookie: u=0ZfsEl8jjh6BR0aoAw9hAg==' -H 'Upgrade-Insecure-Requests: 1' -H 'Sec-Fetch-Dest: document' -H 'Sec-Fetch-Mode: navigate' -H 'Sec-Fetch-Site: cross-site' -H 'Sec-Fetch-User: ?1' -H 'Cache-Control: max-age=0'"
    val x = "curl 'https://localhost:8080/' --http2"
    val p = Curly.all.parse(x)

    IO(println(p)).as(ExitCode.Success)
  }

}


object Curly {
  sealed trait Component
  case class Uri(s: String) extends Component
  case class Method(x: String) extends Component
  case class Header(key: String, value: String) extends Component
  case class Unknown(option: String, value: Option[String]) extends Component

  import cats.parse.Rfc5234.{alpha, sp}
  import cats.parse.Rfc5234
  import cats.parse.{Parser => P, Parser0 => P0}
  

  val curl = sp.? *> P.ignoreCase("curl") *> sp

  val uri = P.char('\'') *> P.until(P.char('\'')) <* P.char('\'') <* sp.?

  val method = {
    (P.string("-X ") | P.string("--request ")) *>
      P.until(sp) <* sp.?
  }.map(Method(_))
  
  val header = {
    val h = P.string("-H ") | P.string("--header ")
    val key = P.char('\'') *> P.until(P.char(':')) <* P.char(':') <* sp.?
    val value = P.until(P.char('\'')) <* P.char('\'') <* sp.?
    h *> (key ~ value).map{ case (a, b) => Header(a, b)}
  }

  val unknown: P[Unknown] = {
    val optDouble = P.string("--") *> (P.not(P.end | sp).with1 ~ P.anyChar).rep.string <* sp.?
    val optSingle = P.string("-") *> P.until(sp) <* sp.?
    val opt = optDouble | optSingle
    val value: P0[Option[String]] = 
      P.peek(P.char('-')).as(Option.empty[String]) |
      (P.until(sp) <* sp.?).map(s => if (s.isEmpty()) None else Some(s))
    (opt ~ value).map{ case (a,b) => Unknown(a, b)}
  }

  val opts: P[Component] = method  | header | unknown

  val all = curl *>
    (uri ~
      unknown
    )

}
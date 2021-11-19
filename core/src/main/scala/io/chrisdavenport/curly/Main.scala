package io.chrisdavenport.curly

import cats.effect._
import cats.syntax.all._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val x = "curl 'https://linux.die.net/man/1/curl' -H 'User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:94.0) Gecko/20100101 Firefox/94.0' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8' -H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'Referer: https://www.google.com/' -H 'Connection: keep-alive' -H 'Cookie: u=0ZfsEl8jjh6BR0aoAw9hAg==' -H 'Upgrade-Insecure-Requests: 1' -H 'Sec-Fetch-Dest: document' -H 'Sec-Fetch-Mode: navigate' -H 'Sec-Fetch-Site: cross-site' -H 'Sec-Fetch-User: ?1' -H 'Cache-Control: max-age=0'"
    // val x = "curl -H 'foo' 'https://localhost:8080/'"
    // val p = Curly.opts.parse("--header 'foo'")
    // val opt = Curly.OptC('H'.some, "header", true)
    // val i = "'https://localhost:8080/'"
    // val p = Curly.opts.parse("-H 'foo'")
    // val p = Curly.uri.parse(i)
    // val p = (Curly.curl *> Curly.uri ~ Curly.opts).parse(x)
    val p = Curly.all.parseAll(x)

    IO(println(p)).as(ExitCode.Success)
  }

}


object Curly {
  sealed trait Component
  case class Uri(s: String) extends Component
  // case class Method(x: String) extends Component
  // case class Header(key: String, value: String) extends Component
  case class Opt(option: String, value: Option[String]) extends Component
  case class Unhandled(value: String) extends Component

  import cats.parse.Rfc5234.{alpha, sp}
  import cats.parse.Rfc5234
  import cats.parse.{Parser => P, Parser0 => P0}
  

  val curl = sp.? *> P.ignoreCase("curl") *> sp

  val uri = P.char('\'') *> P.until(P.char('\'')) <* P.char('\'') <* sp.?

  case class OptC(short: Option[Char], long: String, hasValue: Boolean)

  val optConfigs: List[OptC] = List(
    OptC('a'.some, "append", false),
    OptC('A'.some, "user-agent", true),
    OptC(None, "anyauth", false),
    OptC('b'.some, "cookie", true),
    OptC('B'.some, "use-ascii", false),
    OptC(None, "basic", false),
    OptC(None, "ciphers", true),
    OptC(None, "connect-timeout", true),
    OptC('c'.some, "cookie-jar", true),
    OptC('C'.some, "continue-at", true),
    OptC(None, "create-dirs", false),
    OptC(None, "crlf", false),
    OptC(None, "crlfile", false),
    OptC('d'.some, "data", true),
    OptC(None, "data-binary", true),
    OptC(None, "data-urlencode", true),
    OptC(None, "digest", false),
    OptC(None, "disable-eprt", false),
    OptC(None, "disable-epsv", false),
    OptC('D'.some, "dump-header", true),
    OptC('e'.some, "referer", true),
    OptC(None, "engine", true),
    OptC(None, "environment", false),
    OptC(None, "egd-file", true),
    OptC('E'.some, "cert", true),
    OptC(None, "cert-type", true),
    OptC(None, "ca-cert", true),
    OptC(None, "capath", true),
    OptC('f'.some, "fail", false),
    OptC(None, "ftp-account", false),
    OptC(None, "ftp-method", true),
    OptC(None, "ftp-pasv", false),
    OptC(None, "ftp-alternative-to-user", true),
    OptC(None, "ftp-skip-pasv-ip", false),
    OptC(None, "ftp-ssl", false),
    OptC(None, "ftp-ssl-control", false),
    OptC(None, "ftp-ssl-reqd", false),
    OptC(None, "ftp-ssl-ccc", false),
    OptC(None, "ftp-ssl-ccc-mode", true),
    OptC('F'.some, "form", true),
    OptC(None, "form-string", true),
    OptC('g'.some, "globoff", false),
    OptC('G'.some, "get", false),
    OptC('h'.some, "help", false),
    OptC('H'.some, "header", true),
    OptC(None, "hostpubmd5", true),
    OptC(None, "http2", false),
    OptC(None, "ignore-content-length", false),
    OptC('i'.some, "include", false),
    OptC(None, "interface", true),
    OptC('I'.some, "head", false),
    OptC('j'.some, "junk-session-cookies", false),
    OptC('k'.some, "insecure", false),
    OptC(None, "keepalive-time", true),
    OptC(None, "key", true),
    OptC(None, "key-type", true),
    OptC(None, "krb", true),
    OptC('K'.some, "config", true),
    OptC(None, "libcurl", true),
    OptC(None, "limit-rate", true),
    OptC('l'.some, "list-only", false),
    OptC(None, "local-port", true),
    OptC('L'.some, "location", false),
    OptC(None, "location-trusted", false),
    OptC(None, "max-filesize", true),
    OptC('m'.some, "max-time", true),
    OptC('M'.some, "manual", false),
    OptC('n'.some, "netrc", false),
    OptC(None, "netrc-optional", false),
    OptC(None, "negotiate", false),
    OptC('N'.some, "no-buffer", false),
    OptC(None, "no-keepalive", false),
    OptC(None, "no-sessionid", false),
    OptC(None, "no-proxy", true),
    OptC(None, "ntlm", false),
    OptC('o'.some, "output", true),
    OptC('O'.some, "remote-name", false),
    OptC(None, "remote-name-all", false),
    OptC(None, "pass", true),
    OptC(None, "post301", false),
    OptC(None, "post301", false),
    OptC(None, "proxy-anyauth", false),
    OptC(None, "proxy-basic", false),
    OptC(None, "proxy-digest", false),
    OptC(None, "proxy-negotiate", false),
    OptC(None, "proxy-ntlm", false),
    OptC(None, "proxy-1.0", true),
    OptC('p'.some, "proxytunnel", false),
    OptC(None, "pubkey", true),
    OptC('P'.some, "ftp-port", true),
    OptC('q'.some, "qDontReadCurlRC", false),
    OptC('Q'.some, "quote", true),
    OptC(None, "raw", false),
    OptC('R'.some, "remote-time", false),
    OptC(None, "retry", true),
    OptC(None, "retry-delay", true),
    OptC(None, "retry-max-time", true),
    OptC('s'.some, "silent", false),
    OptC('S'.some, "show-error", false),
    OptC(None, "socks4", true),
    OptC(None, "socks4a", true),
    OptC(None, "socks5-hostname", true),
    OptC(None, "socks5", true),
    OptC(None, "socks5-gssapi-service", true),
    OptC(None, "socks5-gssapi-nec", false),
    OptC(None, "stderr", true),
    OptC(None, "tcp-nodelay", false),
    OptC('t'.some, "telnet-option", true),
    OptC('T'.some, "upload-file", true),
    OptC(None, "trace", true),
    OptC(None, "trace-ascii", true),
    OptC(None, "trace-time", false),
    OptC('u'.some, "user", true),
    OptC('U'.some, "proxy-user", true),
    OptC(None, "url", true),
    OptC('v'.some, "verbose", false),
    OptC('V'.some, "version", false),
    OptC('x'.some, "proxy", true),
    OptC('X'.some, "request", true),
    OptC('y'.some, "speed-time", true),
    OptC('Y'.some, "speed-limit", true),
    OptC('z'.some, "time-cond", true),
    OptC(None, "max-redirs", true),
    OptC('0'.some, "http1.0", false),
    OptC('1'.some, "tslv1", false),
    OptC('2'.some, "sslv2", false),
    OptC('3'.some, "sslv3", false),
    OptC('4'.some, "ipv4", false),
    OptC('6'.some, "ipv6", false),
    OptC('#'.some, "progress-bar", false)
  )

  def known: P[Opt] = {
    optConfigs.foldRight[P[Opt]](P.fail){ case (optC, p) => parseOptC(optC).backtrack | p}
  }

  def parseOptC(optC: OptC): P[Opt] = {
    val short = optC.short match{
      case Some(c) => P.string("-") *> P.char(c) <* sp.?.void
      case None => P.fail
    }
    val long = P.string("--") *> P.string(optC.long) <* sp.?.void
    val code = short | long
    val body = if (optC.hasValue) {
      P.char('$').? *> P.char('\'') *> P.until(P.char('\'')).map(_.some) <* P.char('\'') <* sp.? |
      P.until(sp).map(_.some) <* sp.? 
    } else P.unit.as(None)
    (code ~ body).map{
      case (_, value) => Opt(optC.long, value)
    }
  }

  val unhandled: P[Unhandled] = (
    P.char('$').?.with1 *> P.char('\'') *> P.until(P.char('\'')) <* P.char('\'') <* sp.? |
    P.until(sp) <* sp.? 
  ).map(Unhandled(_))


  val opts: P[Component] = known | unhandled //| method  | header | unknown

  val all = curl *>
    opts.rep0


}
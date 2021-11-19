package io.chrisdavenport.curly

import cats._
import cats.effect._
import cats.syntax.all._
import cats.parse.Rfc5234.{alpha, sp}
import cats.parse.Rfc5234
import cats.parse.{Parser => P, Parser0 => P0}
import cats.ApplicativeError

object CurlyParser {
  sealed trait Opts
  object Opts {
    case class Flag(option: String) extends Opts
    case class Opt(option: String, value: String) extends Opts
    case class Unhandled(value: String) extends Opts
  }

  def parseOpts[F[_]: ApplicativeThrow](input: String): F[List[CurlyParser.Opts]] = {
    CurlyParser.all.parseAll(input) match {
      case Left(value) => 
        val eString = "Error Parsing Components\n" + displayErrorOnString(value, input)
        new Throwable(eString).raiseError[F, List[CurlyParser.Opts]]
      case Right(value) =>
        value.pure[F]
    }
  }


  def debugParse[A](p: P0[A], s: String): IO[Unit] = {
    val res = p.parseAll(s)
    val warn = res match {
      case Left(err) => IO.println(displayErrorOnString(err, s))
      case _ => IO.unit
    }
    warn *> IO.println(res)
  }
  
  def displayErrorOnString(err: P.Error, input: String): String = {
    input + "\n" + ("-" * err.failedAtOffset) + "^"
  }

  val curl = sp.? *> P.ignoreCase("curl") *> sp

  val uri = P.char('\'') *> P.until(P.char('\'')) <* P.char('\'') <* sp.?

  case class OptC(short: Option[Char], long: String, hasValue: Boolean)

  val optConfigs: List[OptC] = List(
    OptC(None, "abstract-unix-socket", true),
    OptC(None, "alt-svc", true),
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
    OptC(None, "cert-status", false),
    OptC(None, "cert-type", true),
    OptC(None, "compressed-ssh", false),
    OptC(None, "compressed", false),
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

  def known: P[Opts] = {
    optConfigs.foldRight[P[Opts]](P.fail){ case (optC, p) => parseOptC(optC).backtrack | p}
  }

  def parseOptC(optC: OptC): P[Opts] = {
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
    (code *> body).map{
      case Some(value) => Opts.Opt(optC.long, value)
      case None => Opts.Flag(optC.long)
    }
  }

  val unhandled: P[Opts.Unhandled] = (
    P.char('$').?.with1 *> P.char('\'') *> P.until(P.char('\'')) <* P.char('\'') <* sp.? |
    P.until(sp) <* sp.? 
  ).map(Opts.Unhandled(_))


  val opts: P[Opts] = known | unhandled //| method  | header | unknown

  val all = curl *>
    opts.rep0
}
package io.chrisdavenport.curly

import cats._
import cats.effect._
import cats.syntax.all._
import cats.parse.Rfc5234.{alpha, sp}
import cats.parse.Rfc5234
import cats.parse.{Parser => P, Parser0 => P0}
import cats.ApplicativeError

object Curly extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val test = for {
      command <- parseArgs(args)
      opts <- CurlyParser.parseOpts[IO](command)
      data <- CurlyData.fromOpts[IO](opts)
      (_, s) <- CurlyHttp4s.fromData[IO](data)
      _ <- IO.println(s)
    } yield () 
    
    test.as(ExitCode.Success)
  }

  def parseArgs(args: List[String]): IO[String] = args match {
    case head :: Nil => head.pure[IO]
    case Nil => IO.raiseError(new Throwable("No Command Line Arguments Provided"))
    case otherwise => IO.raiseError(new Throwable("Incorrect Number of Command Line Arguments"))
  }

}



// Example Invocation
//
// sbt -warn "coreJVM/run \"curl 'https://linux.die.net/man/1/curl' -H 'User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:94.0) Gecko/20100101 Firefox/94.0' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8' -H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'Referer: https://www.google.com/' -H 'Connection: keep-alive' -H 'Cookie: u=0ZfsEl8jjh6BR0aoAw9hAg==' -H 'Upgrade-Insecure-Requests: 1' -H 'Sec-Fetch-Dest: document' -H 'Sec-Fetch-Mode: navigate' -H 'Sec-Fetch-Site: cross-site' -H 'Sec-Fetch-User: ?1' -H 'Cache-Control: max-age=0'\""

// Example Command Line
// curly "curl 'https://www.icanhazdadjoke.com' -H 'Accept: application/json'"

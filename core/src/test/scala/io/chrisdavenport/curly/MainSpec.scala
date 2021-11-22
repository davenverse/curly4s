package io.chrisdavenport.curly


class MainSpec extends munit.FunSuite {
  import CurlyParser._
  import Opts._

  test("leading 'curl' is case insensitive") {
    val cmd = "CuRl 'https://typelevel.org/'"
    val actual = CurlyParser.all.parseAll(cmd)
    val expected = List(Unhandled("https://typelevel.org/"))
    assertEquals(actual, Right(expected))
  }

  test("only a URL") {
    val cmd = "curl 'https://typelevel.org/'"
    val actual = CurlyParser.all.parseAll(cmd)
    val expected = List(Unhandled("https://typelevel.org/"))
    assertEquals(actual, Right(expected))
  }

  test("with one header") {
    val cmd = "curl 'https://typelevel.org/' -H 'User-Agent: Mozilla/5.0'"
    val actual = CurlyParser.all.parseAll(cmd)
    val expected = List(
      Unhandled("https://typelevel.org/"),
      Opt("header", "User-Agent: Mozilla/5.0"),
    )
    assertEquals(actual, Right(expected))
  }

  test("with two headers") {
    val cmd = "curl 'https://typelevel.org/' -H 'User-Agent: Mozilla/5.0' -H 'Connection: keep-alive'"
    val actual = CurlyParser.all.parseAll(cmd)
    val expected = List(
      Unhandled("https://typelevel.org/"),
      Opt("header", "User-Agent: Mozilla/5.0"),
      Opt("header", "Connection: keep-alive"),
    )
    assertEquals(actual, Right(expected))
  }

  test("with two headers separated by a long arg") {
    val cmd = "curl 'https://typelevel.org/' -H 'User-Agent: Mozilla/5.0' --compressed -H 'Connection: keep-alive'"
    val actual = CurlyParser.all.parseAll(cmd)
    val expected = List(
      Unhandled("https://typelevel.org/"),
      Opt("header", "User-Agent: Mozilla/5.0"),
      Flag("compressed"),
      Opt("header", "Connection: keep-alive"),
    )
    assertEquals(actual, Right(expected))
  }

  test("with slash new line notation") {
    val cmd = """curl 'https://typelevel.org/' \
    |  -H 'User-Agent: Mozilla/5.0' \
    |  --compress \
    |  -H 'Connection: keep-alive'""".stripMargin
    val actual = CurlyParser.all.parseAll(cmd)
    assertEquals(actual.map(_.length), Right(4))
  }

}

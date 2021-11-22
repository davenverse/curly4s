package io.chrisdavenport.curly


class MainSpec extends munit.FunSuite {
  import CurlyParser._

  test("leading 'curl' is case insensitive") {
    val cmd = "CuRl 'https://typelevel.org/'"
    val actual = CurlyParser.all.parseAll(cmd)
    // TODO actually a URI
    val expected = List(Opts.Unhandled("https://typelevel.org/"))
    assertEquals(actual, Right(expected))
  }

  test("only a URL") {
    val cmd = "curl 'https://typelevel.org/'"
    val actual = CurlyParser.all.parseAll(cmd)
    // TODO actually a URI
    val expected = List(Opts.Unhandled("https://typelevel.org/"))
    assertEquals(actual, Right(expected))
  }

  //
  // Just asserting lengths for now...
  //

  test("with one header") {
    val cmd = "curl 'https://typelevel.org/' -H 'User-Agent: Mozilla/5.0'"
    val actual = CurlyParser.all.parseAll(cmd)
    assertEquals(actual.map(_.length), Right(2))
  }

  test("with two headers") {
    val cmd = "curl 'https://typelevel.org/' -H 'User-Agent: Mozilla/5.0' -H 'Connection: keep-alive'"
    val actual = CurlyParser.all.parseAll(cmd)
    assertEquals(actual.map(_.length), Right(3))
  }

  test("with two headers separated by a long arg") {
    val cmd = "curl 'https://typelevel.org/' -H 'User-Agent: Mozilla/5.0' --compress -H 'Connection: keep-alive'"
    val actual = CurlyParser.all.parseAll(cmd)
    assertEquals(actual.map(_.length), Right(4))
  }

  test("with slash new line notation") {
    val cmd = """curl 'https://typelevel.org/' \  -H 'User-Agent: Mozilla/5.0' \  --compress \  -H 'Connection: keep-alive'"""
    val actual = CurlyParser.all.parseAll(cmd)
    assertEquals(actual.map(_.length), Right(4))
  }

}

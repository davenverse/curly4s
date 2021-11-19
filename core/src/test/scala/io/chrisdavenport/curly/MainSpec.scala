package io.chrisdavenport.curly

import munit.CatsEffectSuite
import cats.effect._

class MainSpec extends CatsEffectSuite {

  test("Main should exit succesfully") {
    assert(true, "Cool")
  }

}

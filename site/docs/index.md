---
layout: home

---

# curly4s - Oh! A WISE guy, eh? [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/curly_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/curly_2.13)

## Quick Start

```scala mdoc:js:invisible
<div>
  <input type="text" id="curl" size="128"/>
  <button id="button" disabled>Run Curly4s</button>
</div>

<script src="https://scastie.scala-lang.org/embedded.js"></script>
<pre id="output" class="output"></pre> 
---
import cats.syntax.all._
import cats.effect._
import cats.effect.unsafe.implicits._
import org.scalajs.dom._
import io.chrisdavenport.curly._
import scala.scalajs.js

val inputElement = document.getElementById("curl").asInstanceOf[html.Input]
val outputElement = document.getElementById("output")
val button = document.getElementById("button").asInstanceOf[html.Button]

window.addEventListener("load", (_: Any) => button.disabled = false)

val process = for {
  command <- IO(inputElement.value)
  opts <- CurlyParser.parseOpts[IO](command)
  data <- CurlyData.fromOpts[IO](opts)
  (_, s) <- CurlyHttp4s.fromData[IO](data)
} yield s

def handleErrors(io: IO[Unit]): IO[Unit] = 
  io.handleErrorWith(e => IO(outputElement.innerHTML = e.toString))

def clearOutput: IO[Unit] =
  IO(outputElement.innerHTML = "") >>
    IO(outputElement.asInstanceOf[js.Dynamic].style.display = "") >>
    IO(document.getElementsByClassName("scastie")(0).asInstanceOf[js.UndefOr[Element]].foreach(_.remove()))

def setOutput(code: String): IO[Unit] = IO {
    js.Dynamic.global.scastie.Embedded(
      "#output",
      js.Dynamic.literal(
        code = "\n" + code,
        sbtConfig = """libraryDependencies += "org.http4s" %% "http4s-core" % "0.23.6"""",
        scalaVersion = "2.13.7",
        targetType = "jvm"
      )
    )
  }

def run = clearOutput >> handleErrors(process.flatMap(setOutput))

button.onclick = _ => run.unsafeRunAndForget()
```
---
layout: home

---

# curly4s - Oh! A WISE guy, eh? [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/curly_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/curly_2.13)

## Quick Start

```scala mdoc:js:invisible
<input type="text" id="curl" style="height:120px; width:200px;">
<button id="button">Run Curly4s</button>
<br>
<div id="output"></div> 
---
import cats.syntax.all._
import cats.effect._
import cats.effect.unsafe.implicits._
import org.scalajs.dom._
import io.chrisdavenport.curly._

val inputElement = document.getElementById("curl").asInstanceOf[html.Input]
val outputElement = document.getElementById("output")
val button = document.getElementById("button").asInstanceOf[html.Button]

val process = for {
  command <- IO(inputElement.value)
  opts <- CurlyParser.parseOpts[IO](command)
  data <- CurlyData.fromOpts[IO](opts)
  (_, s) <- CurlyHttp4s.fromData[IO](data)
} yield s

def handleErrors(io: IO[String]): IO[String] = 
  io.handleErrorWith(e => e.toString.pure[IO])

def setOutput(s: String): IO[Unit] = IO(outputElement.innerHTML = s)

def run = handleErrors(process).flatMap(setOutput)

button.onclick = _ => run.unsafeRunAndForget()
```
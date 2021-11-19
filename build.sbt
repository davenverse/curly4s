import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Scala213 = "2.13.6"

ThisBuild / crossScalaVersions := Seq("2.12.14", Scala213)
ThisBuild / scalaVersion := Scala213

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

val catsV = "2.6.1"
val catsParseV = "0.3.5"
val http4sV = "0.23.6"
val munitCatsEffectV = "1.0.6"


// Projects
lazy val `curly` = project.in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .aggregate(core.jvm, core.js)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "curly",

    libraryDependencies ++= Seq(
      "org.typelevel"               %%% "cats-core"                  % catsV,
      // "org.typelevel"               %% "cats-effect"                % catsEffectV,

      // "co.fs2"                      %% "fs2-core"                   % fs2V,
      // "co.fs2"                      %% "fs2-io"                     % fs2V,

      "org.http4s"                  %%% "http4s-core"                 % http4sV,
      "org.typelevel"               %%% "cats-parse"                  % catsParseV,
      // "org.http4s"                  %% "http4s-ember-server"        % http4sV,
      // "org.http4s"                  %% "http4s-ember-client"        % http4sV,
      // "org.http4s"                  %% "http4s-circe"               % http4sV,

      "org.typelevel"               %%% "munit-cats-effect-3"        % munitCatsEffectV         % Test,

    )
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule)},
  )

lazy val site = project.in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(DavenverseMicrositePlugin)
  .dependsOn(core.jvm)
  .settings{
    import microsites._
    Seq(
      micrositeDescription := "Oh! A WISE guy, eh?",
    )
  }

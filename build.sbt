ThisBuild / tlBaseVersion := "0.1" // current series x.y

// Nothing in the 0.1 series has ever reached Maven Central: v0.1.0 and v0.1.1
// were both tagged but published nothing (a stale ci-release publish step, then
// this very MiMa lookup). The last real release is 0.0.8, in the previous
// series, so there is no valid baseline for 0.1.x.
ThisBuild / tlMimaPreviousVersions := Set.empty

ThisBuild / organization := "io.chrisdavenport"
ThisBuild / organizationName := "Christopher Davenport"
ThisBuild / startYear := Some(2021)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  tlGitHubDev("christopherdavenport", "Christopher Davenport")
)

// sbt-davenverse published a snapshot from main on every push; preserve that.
ThisBuild / tlCiReleaseBranches := Seq("main")

val Scala213tl = "2.13.18"
ThisBuild / crossScalaVersions := Seq("2.12.20",  Scala213tl)
ThisBuild / scalaVersion := Scala213tl

// Compiler settings DavenversePlugin injected globally. sbt-typelevel-ci-release
// does not supply these (only sbt-typelevel-settings would). Scoped to ThisBuild
// so every project picks them up without editing each one.
ThisBuild / libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, _)) =>
    Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % "0.13.4" cross CrossVersion.full),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    )
  case _ => Nil
})
ThisBuild / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((3, _)) => Seq("-Ykind-projector")
  case Some((2, 12)) => Seq("-Ypartial-unification")
  case _ => Nil
})

import org.scalajs.sbtplugin.Stage

val Scala213 = "2.13.7"


ThisBuild / testFrameworks += new TestFramework("munit.Framework")

ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowBuildPreamble ++= Seq(WorkflowStep.Use(
  UseRef.Public("actions", "setup-node", "v1"),
  Map(
    "node-version" -> "14"
  )
))

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    List("test", "coreJS/npmPackageInstall"),
    name = Some("Install artifacts to npm"),
  )
)

ThisBuild / githubWorkflowPublishPreamble ++= Seq(
  WorkflowStep.Use(
    UseRef.Public("actions", "setup-node", "v1"),
    Map(
      "node-version" -> "14",
    ),
  )
)

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("tlCiRelease"),
    name = Some("Publish artifacts to Sonatype"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}",
      "NPM_TOKEN" -> "${{ secrets.NPM_TOKEN }}" // in scope so the npm publish below can reuse it
    )
  ),
  WorkflowStep.Sbt(
    List("npmPackageNpmrc", "npmPackagePublish"),
    name = Some("Publish artifacts to npm"),
    env = Map(
      "NPM_TOKEN" -> "${{ secrets.NPM_TOKEN }}"
    )
  )
)


val catsV = "2.6.1"
val catsEffectV = "3.2.9"
val catsParseV = "0.3.6"
val http4sV = "0.23.6"
val munitCatsEffectV = "1.0.6"

// Projects
lazy val `curly` = project.in(file("."))
    .enablePlugins(NoPublishPlugin)
  .aggregate(core.jvm, core.js)

// import .util.JSON._
lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .jsEnablePlugins(NpmPackagePlugin)
  .settings(
    name := "curly4s",

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"           % catsV,
      "org.typelevel" %%% "cats-effect"         % catsEffectV,
      "org.typelevel" %%% "cats-parse"          % catsParseV,
      "org.http4s"    %%% "http4s-core"         % http4sV,
      "org.typelevel" %%% "munit-cats-effect-3" % munitCatsEffectV % Test,
    ),

  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule)},
    scalaJSUseMainModuleInitializer := true,
    scalaJSStage in Global := FullOptStage,
    npmPackageAuthor := "Christopher Davenport",
    npmPackageDescription := "Curl Command Line Parser which outputs http4s code",
    npmPackageKeywords := Seq(
      "curl",
      "command line",
      "http4s"
    ),
    npmPackageStage := Stage.FullOpt,
    npmPackageBinaryEnable := true,
  )

lazy val jsdocs = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core.js)
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    evictionErrorLevel := sbt.util.Level.Info,

  )

lazy val site = project.in(file("site"))
    .enablePlugins(TypelevelSitePlugin)
  .settings(
    laikaTheme := tlSiteHelium.value.site
      .topNavigationBar(
        homeLink = laika.helium.config.IconLink.internal(laika.ast.Path.Root / "index.md", laika.helium.config.HeliumIcon.home)
      )
      .build
  )
  .settings{
    Seq(
      mdocJS := Some(jsdocs),
    )
  }

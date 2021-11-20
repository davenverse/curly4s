import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import org.scalajs.sbtplugin.Stage

val Scala213 = "2.13.7"

ThisBuild / crossScalaVersions := Seq("2.12.14", Scala213)
ThisBuild / scalaVersion := Scala213

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

ThisBuild / githubWorkflowPublish ++= Seq(
  WorkflowStep.Sbt(
    List("coreJS/npmPackageNpmrc", "coreJS/npmPackagePublish"),
    name = Some("Publish artifacts to npm"),
    env = Map(
      "NPM_TOKEN" -> "${{ secrets.NPM_TOKEN }}" // https://docs.npmjs.com/using-private-packages-in-a-ci-cd-workflow#set-the-token-as-an-environment-variable-on-the-cicd-server
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
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .aggregate(core.jvm, core.js)

// import .util.JSON._
lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .jsEnablePlugins(NpmPackagePlugin)
  .in(file("core"))
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

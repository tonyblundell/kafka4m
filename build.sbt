import org.scoverage.coveralls.Imports.CoverallsKeys._

name := "kafka4m"

organization := "com.github.aaronp"

enablePlugins(GhpagesPlugin)
enablePlugins(ParadoxPlugin)
enablePlugins(SiteScaladocPlugin)
enablePlugins(ParadoxMaterialThemePlugin) // see https://jonas.github.io/paradox-material-theme/getting-started.html

//ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox)

val username            = "aaronp"
val scalaTwelve         = "2.12.10"
val defaultScalaVersion = scalaTwelve
crossScalaVersions := Seq(scalaTwelve)

paradoxProperties += ("project.url" -> "https://aaronp.github.io/kafka4m/docs/current/")

Compile / paradoxMaterialTheme ~= {
  _.withLanguage(java.util.Locale.ENGLISH)
    .withColor("red", "orange")
    .withLogoIcon("cloud")
    .withRepository(uri("https://github.com/aaronp/kafka4m"))
    .withSocial(uri("https://github.com/aaronp"))
    .withoutSearch()
}

//scalacOptions += Seq("-encoding", "UTF-8")

siteSourceDirectory := target.value / "paradox" / "site" / "main"

siteSubdirName in SiteScaladoc := "api/latest"

libraryDependencies ++= List(
  "io.monix"                   %% "monix"          % "3.0.0",
  "io.monix"                   %% "monix-reactive" % "3.0.0",
  "io.monix"                   %% "monix-eval"     % "3.0.0",
  "com.lihaoyi"                %% "sourcecode"     % "0.1.7",
  "com.github.aaronp"          %% "args4c"         % "0.6.6",
  "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.2",
  "ch.qos.logback"             % "logback-classic" % "1.2.3",
  "com.typesafe"               % "config"          % "1.3.3",
  "org.apache.kafka"           % "kafka-clients"   % "2.3.0",
  "org.apache.kafka"           % "kafka-streams"   % "2.3.0"
)

libraryDependencies ++= List(
  "org.scalactic"     %% "scalactic" % "3.0.4" % "test",
  "org.scalatest"     %% "scalatest" % "3.0.4" % "test",
  "org.pegdown"       % "pegdown"    % "1.6.0" % "test",
  "junit"             % "junit"      % "4.12"  % "test",
  "com.github.aaronp" %% "dockerenv" % "0.0.4" % "test",
  "com.github.aaronp" %% "dockerenv" % "0.0.4" % "test" classifier ("tests")
)

publishMavenStyle := true
releaseCrossBuild := true
coverageMinimum := 90
coverageFailOnMinimum := true
git.remoteRepo := s"git@github.com:$username/kafka4m.git"
ghpagesNoJekyll := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

test in assembly := {}
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

// https://coveralls.io/github/aaronp/kafka4m
// https://github.com/scoverage/sbt-coveralls#specifying-your-repo-token
coverallsTokenFile := Option((Path.userHome / ".sbt" / ".coveralls.kafka4m").asPath.toString)

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "kafka4m.build"

// see http://scalameta.org/scalafmt/
scalafmtOnCompile in ThisBuild := true
scalafmtVersion in ThisBuild := "1.4.0"

// see http://www.scalatest.org/user_guide/using_scalatest_with_sbt
testOptions in Test += (Tests.Argument(TestFrameworks.ScalaTest, "-h", s"target/scalatest-reports", "-oN"))

pomExtra := {
  <url>https://github.com/aaronp/kafka4m</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>Aaron</id>
        <name>Aaron Pritzlaff</name>
        <url>http://github.com/aaronp</url>
      </developer>
    </developers>
}

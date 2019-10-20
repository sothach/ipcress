name := "ipcress"
 
version := "1.0"
maintainer :=  "phillips.roy@gmail.com"

lazy val `ipcress` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  guice,
  ws,
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test,
  "org.mockito" % "mockito-all" % "2.0.2-beta" % Test)

enablePlugins(DockerPlugin)
val repoUser = "sothach"
val repoName = "ipcress"
val repo = "dscr.io"
javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)
maintainer in Docker := maintainer.toString()
dockerUsername := Some(repoUser)
dockerRepository := Some(s"$repo/$repoUser")
dockerAlias := DockerAlias(Some(repo),Some(repoUser),repoName,Some("latest"))
dockerUpdateLatest := true
dockerExposedPorts ++= Seq(9000)
dockerExposedVolumes := Seq("/opt/docker/logs")

coverageExcludedPackages := "<empty>;controllers.Reverse.*;router.*;controllers.javascript;play.api.*;views.html.*"
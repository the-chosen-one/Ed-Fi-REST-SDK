import xml.Group
import AssemblyKeys._

organization := "org.ed-fi"

name := "sdk-generate"

version := "1.0.0"

//scalaVersion := "2.10.3"

scalaHome := Some(file("./scala-libraries"))

javacOptions ++= Seq("-target", "1.6", "-source", "1.6", "-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-optimize", "-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8")

crossScalaVersions := Seq("2.9.0", "2.9.0-1", "2.9.1", "2.9.1-1", "2.9.2", "2.9.3", "2.10.0")

libraryDependencies ++= Seq(
  "org.json4s"                  %% "json4s-jackson"     % "3.2.5",
  "io.backchat.inflector"       %% "scala-inflector"    % "1.3.5",
  "commons-io"                   % "commons-io"         % "2.3",
  "ch.qos.logback"               % "logback-classic"    % "1.0.13",
  "org.rogach"                  %% "scallop"            % "0.9.4",
  "junit"                        % "junit"              % "4.11" % "test",
  "org.scalatest"               %% "scalatest"          % "1.9.1" % "test"
)

// add scopt for CLI parsing

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

resolvers += Resolver.sonatypeRepo("public")


libraryDependencies <+= scalaVersion {
  case v if v.startsWith("2.9") => 
    "org.fusesource.scalate" % "scalate-core_2.9" % "1.6.1"
  case v if v.startsWith("2.10") => 
    "org.fusesource.scalate" %% "scalate-core" % "1.6.1"
}

packageOptions <+= (name, version, organization) map {
  (title, version, vendor) =>
    Package.ManifestAttributes(
      "Created-By" -> "Simple Build Tool",
      "Built-By" -> "DLP Developer",
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> title,
      "Specification-Version" -> version,
      "Specification-Vendor" -> vendor,
      "Implementation-Title" -> title,
      "Implementation-Version" -> version,
      "Implementation-Vendor-Id" -> vendor,
      "Implementation-Vendor" -> vendor
    )
}

publishTo <<= (version) { version: String =>
  if (version.trim.endsWith("SNAPSHOT"))
    Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
  else
    Some("Sonatype Nexus Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}


//publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))


publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

homepage := Some(new URL("http://www.ed-fi.org"))

parallelExecution in Test := false

startYear := Some(2014)

licenses := Seq(("Apache License 2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0.html")))

assemblySettings

jarName in assembly := "sdk-generate.jar"

mainClass in assembly := Some("org.edfi.restsdk.EdFiRestGeneratorLauncher")

//import play.sbt.PlayScala

organization  := "org.alfresco"

version       := "1.0-SNAPSHOT"

scalaVersion  := "2.11.6"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

// For SNAPSHOT releases
//resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

//resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

resolvers ++= Seq(
  "snapshots" at "http://scala-tools.org/repo-snapshots",
  "releases" at "http://scala-tools.org/repo-releases",
  "alfresco" at "https://artefacts.alfresco.com/nexus/content/groups/public",
  "alfresco-internal-snapshots" at "https://artefacts.alfresco.com/nexus/content/repositories/internal-snapshots",
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/internal-snapshots/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
//  Resolver.bintrayRepo("websudos", "oss-releases")
)

libraryDependencies ++= {
  Seq(
    "org.scalatest"       %%  "scalatest"             % "2.2.4"  % "test",
    "org.alfresco.services" % "alfresco-events" % "1.0-SNAPSHOT" exclude("com.fasterxml.jackson.core", "jackson-databind"),
//    "org.apache.camel" % "camel-jackson" % "2.13.2",
    "org.apache.camel" % "camel-core" % "2.13.2",
    "org.apache.camel" % "camel-amqp" % "2.13.2",
    "org.apache.camel" % "camel-scala" % "2.13.2",
    "org.apache.activemq" % "activemq-core" % "5.7.0",
    "org.apache.activemq" % "activemq-amqp" % "5.10.0",
    "org.apache.qpid" % "qpid-amqp-1-0-client-jms" % "0.26",
    "org.apache.activemq" % "activemq-camel" % "5.11.1",
    "org.apache.camel" % "camel-jms" % "2.13.2",
    "ch.qos.logback" % "logback-core" % "1.0.13",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "com.fasterxml.jackson.core" % "jackson-core" % "2.6.3",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.0-1"
  )
}

test in assembly := {}

lazy val root = (project in file(".")).
  settings(
    name := "EventControl",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.10.4",
    mainClass in Compile := Some("org.alfresco.event.Process")        
  )
  //.enablePlugins(PlayScala)

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

scalacOptions in Test ++= Seq("-Yrangepos")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

doc in Compile <<= target.map(_ / "none")

publishArtifact in (Compile, packageSrc) := false

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false

fork in run := true

enablePlugins(JavaServerAppPackaging)

//mergeStrategy in assembly := {
assemblyMergeStrategy in assembly <<= (assemblyMergeStrategy in assembly) { (old) => 
{
  case n PathList("META-INF", "ECLIPSEF.INF") => MergeStrategy.first
  case n PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.first
  case n PathList("META-INF", "ECLIPSE_.RSA") => MergeStrategy.first
  case n PathList("META-INF", "ECLIPSEF.SF") => MergeStrategy.first
//  case n if n.toLowerCase.endsWith("eclipse.inf") => MergeStrategy.first 
//  case n if n.toLowerCase.endsWith("eclipsef.rsa") => MergeStrategy.first 
//  case n if n.toLowerCase.endsWith("eclipse_.rsa") => MergeStrategy.first 
//  case n if n.toLowerCase.endsWith("eclipsef.sf") => MergeStrategy.first 
  case n if n.startsWith("META-INF/MANIFEST.MF") => MergeStrategy.discard
  case n if n.startsWith("META-INF/NOTICE.txt") => MergeStrategy.discard
  case n if n.startsWith("META-INF/NOTICE") => MergeStrategy.discard
  case n if n.startsWith("META-INF/LICENSE.txt") => MergeStrategy.discard
  case n if n.startsWith("META-INF/LICENSE") => MergeStrategy.discard
  case n if n.startsWith("rootdoc.txt") => MergeStrategy.discard
  case n if n.startsWith("readme.html") => MergeStrategy.discard
  case n if n.startsWith("readme.txt") => MergeStrategy.discard
  case n if n.startsWith("library.properties") => MergeStrategy.discard
  case n if n.startsWith("license.html") => MergeStrategy.discard
  case n if n.startsWith("about.html") => MergeStrategy.discard
  case n if n.startsWith("META-INF/spring.schemas") => MergeStrategy.discard
  case n if n.startsWith("META-INF/spring.handlers") => MergeStrategy.discard
  case n if n.startsWith("META-INF/spring.tooling") => MergeStrategy.last
  case n if n.startsWith("META-INF/spring.factories") => MergeStrategy.discard
  case n if n.startsWith("META-INF/services/org/apache/camel/component.properties") => MergeStrategy.discard
  case n if n.startsWith("META-INF/io.netty.versions.properties") => MergeStrategy.discard
  case n if n.startsWith("META-INF/DEPENDENCIES") => MergeStrategy.discard
  case n if n.startsWith("META-INF/INDEX.LIST") => MergeStrategy.discard
  case n if n.startsWith("reference.conf") => MergeStrategy.discard
  case n if n.startsWith("overview.html") => MergeStrategy.discard
  case n if n.toLowerCase.matches("meta-inf/.*\\.sf$") => MergeStrategy.discard
  case n if n.toLowerCase.endsWith(".sf") => MergeStrategy.discard
  case n if n.toLowerCase.endsWith(".dsa") => MergeStrategy.discard
  case n if n.toLowerCase.endsWith(".rsa") => MergeStrategy.discard
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
//  case PathList("org", "apache", "commons", "beanutils") => MergeStrategy.first
//  case _ => MergeStrategy.deduplicate
  case x => old(x)
  }
}

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {_.data.getName == "guava-16.0.1.jar"}
}

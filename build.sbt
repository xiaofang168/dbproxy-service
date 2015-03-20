import AssemblyKeys._

seq(assemblySettings: _*)

name := "dbproxy-services"

organization  := "com.hikvision.dbproxy"

version       := "0.1"

scalaVersion  := "2.10.3"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7")

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.7", "-target", "1.7")

compileOrder := CompileOrder.JavaThenScala

libraryDependencies ++= {
  Seq(
	"com.hikvision.dbproxy"   %%       "dbproxy-entities"   % "0.1-SNAPSHOT" withSources(),
	"com.hikvision.dbproxy"   %%  	   "dbproxy-cache"      % "1.0" withSources(),
	"com.hikvision.dbproxy"    %	   "dbproxy-core"       % "1.0" withSources(),
	"com.hikvision.dbproxy"    %       "dbproxy-jdbc"       % "1.0" withSources(),
	"commons-dbcp" % "commons-dbcp" % "1.4" withSources(),
	"io.spray" 			  %%  "spray-json"    % "1.2.6" withSources(),
	"mysql" 			% 	"mysql-connector-java" % "5.1.30",
	"com.microsoft.sqlserver.jdbc" % "sqljdbc4"% "1.0",
	"com.typesafe.akka" %% "akka-remote" % "2.3.4",
	"org.scala-lang"   %       "scala-compiler"  % "2.10.3",
	"com.twitter"      %%  	   "util-eval"       % "6.12.1" withSources(),
	"org.apache.httpcomponents" % "httpclient" % "4.3" withSources(),
	"junit" % "junit" % "4.4" % "test"
  )
}

mainClass in (Compile, packageBin) := Some("com.hikvision.dbproxy.services.Boot")

jarName in assembly := "dbproxy-service.jar"

mainClass in assembly := Some("com.hikvision.dbproxy.services.Boot")
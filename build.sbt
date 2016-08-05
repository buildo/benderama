libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"        % "2.4.+",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.4.+",
  "ch.qos.logback"  %  "logback-classic"    % "1.1.+",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "com.chuusai" %% "shapeless" % "2.3.1",
  "com.github.nscala-time" %% "nscala-time" % "2.12.0",
  "com.typesafe" % "config" % "1.3.0",
  "com.github.philcali" %% "cronish" % "0.1.3",
  "org.scalanlp" %% "breeze" % "0.12"
)

scalacOptions ++= Seq("-feature", "-deprecation", "-Yresolve-term-conflict:package")

name := "PredictionIO Connection Check Tool"

version := "0.4-SNAPSHOT"

organization := "io.prediction"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  "io.prediction" %% "predictionio-commons" % "0.4-SNAPSHOT"
)

resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

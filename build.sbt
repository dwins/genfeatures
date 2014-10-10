name := "gengeom"

version := "0.1"

scalaVersion := "2.11.2"

organization := "com.boundlessgeo"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Boundless Repository" at "http://repo.boundlessgeo.com/main",
  "OSGeo Repository" at "http://download.osgeo.org/webdav/geotools/",
  "Oracle Releases" at "http://download.oracle.com/maven",
  "geosolutions repo" at "http://maven.geo-solutions.it/",
  "spray repo" at "http://repo.spray.io/"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= {
  val geotoolsVersion = "10.5"
  Seq(
    "org.geotools" % "gt-main" % geotoolsVersion,
    "org.geotools" % "gt-shapefile" % geotoolsVersion,
    "org.geotools" % "gt-epsg-hsql" % geotoolsVersion,
    "org.scalacheck" %% "scalacheck" % "1.11.6"
  )
}

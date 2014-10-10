package genfeatures

import com.vividsolutions.jts.geom.Point
import org.scalacheck.{ Arbitrary, Gen }
import org.scalacheck.Arbitrary.arbitrary

object GenShapefile extends App {
  val geometries = GeometryGenerators.Default
  import geometries._
  implicit val arbInteger = Arbitrary { arbitrary[Int].map(java.lang.Integer.valueOf _) }

  val schema = Schema("test", Seq("the_geom".as[Point], "label".as[String], "value".as[java.lang.Integer], "count".as[java.lang.Integer]))
  val store = getShapefileStore("file:./")
  generateNFeatures(store, schema, 1000)
}

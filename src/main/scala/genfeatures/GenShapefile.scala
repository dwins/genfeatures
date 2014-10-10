package genfeatures

import com.vividsolutions.jts.geom.Point
import org.scalacheck.{ Arbitrary, Gen }
import org.scalacheck.Arbitrary.arbitrary

object GenShapefile extends App {
  val fac = new com.vividsolutions.jts.geom.GeometryFactory
  implicit val arbPoint = Arbitrary {
    for {
      x <- Gen.chooseNum(-180d, 180d)
      y <- Gen.chooseNum(-180d, 180d)
    } yield fac.createPoint(new com.vividsolutions.jts.geom.Coordinate(x, y))
  }

  implicit val arbInteger = Arbitrary { arbitrary[Int].map(java.lang.Integer.valueOf _) }
  val schema = Schema("test", Seq("the_geom".as[Point], "label".as[String], "value".as[java.lang.Integer]))

  val dsFactory = new org.geotools.data.shapefile.ShapefileDataStoreFactory
  val store = dsFactory.createDataStore(new java.net.URL("file:test.shp"))
  store.createSchema(schema.toFeatureType)
  val tx = new org.geotools.data.DefaultTransaction
  val writer = store.getFeatureWriter("file:test", tx)
  val features = schema.features
  for (_ <- 0 until 100) {
    val f = features.sample
    val toWrite = writer.next
    toWrite.setAttributes(f.get.getAttributes)
    writer.write
  } 
  writer.close()
  tx.commit()
  tx.close()
  store.dispose()
}

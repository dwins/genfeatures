import org.scalacheck.{ Arbitrary, Gen }
import scala.collection.JavaConverters._

package genfeatures {
  case class Column[A](name: String, gen: Gen[A], binding: Class[A])

  object Column {
    def apply[A : Arbitrary : Manifest](name: String): Column[A] = {
      new Column(name, Arbitrary.arbitrary, manifest[A].runtimeClass.asInstanceOf[Class[A]])
    }
  }

  case class Schema(name: String, columns: Seq[Column[_]]) {
    def features: Gen[org.opengis.feature.simple.SimpleFeature] = {
      val schema = toFeatureType
      for (fields <- sequence(columns map (_.gen))) 
      yield org.geotools.feature.simple.SimpleFeatureBuilder.build(schema, fields.asInstanceOf[Seq[Object]].toArray, null)
    }

    def toFeatureType: org.opengis.feature.simple.SimpleFeatureType = {
      val builder = new org.geotools.feature.simple.SimpleFeatureTypeBuilder
      builder.setName(name)
      columns.foreach { b => builder.add(b.name, b.binding) }
      builder.buildFeatureType
    }
  }

  sealed trait GeometryGenerators {
    def fac: com.vividsolutions.jts.geom.GeometryFactory
    implicit val arbPoint = Arbitrary {
      for {
        x <- Gen.chooseNum(-180d, 180d)
        y <- Gen.chooseNum(-180d, 180d)
      } yield fac.createPoint(new com.vividsolutions.jts.geom.Coordinate(x, y))
    }
  }

  object GeometryGenerators {
    def Default = new GeometryGenerators { def fac = new com.vividsolutions.jts.geom.GeometryFactory }
  }
}

package object genfeatures {
  implicit class ColumnSugar(name: String) extends AnyRef {
    def as[A : Arbitrary : Manifest]: Column[A] = Column(name)
    def from[A : Manifest](gen: Gen[A]): Column[A] = Column(name, gen, manifest[A].runtimeClass.asInstanceOf[Class[A]])
  }

  def sequence[A](gens: Seq[Gen[A]]): Gen[Seq[A]] = {
    gens.foldLeft(Gen.const(Seq.empty[A])) { (gen, elem) => 
      for {
        g <- gen
        e <- elem
      } yield g :+ e
    }
  }

  def getShapefileStore(path: String): org.geotools.data.DataStore = {
    val dsFactory = new org.geotools.data.shapefile.ShapefileDirectoryFactory
    val params = Map[String, java.io.Serializable]("url" -> new java.net.URL("file:./")).asJava
    dsFactory.createDataStore(params)
  }

  def generateNFeatures(store: org.geotools.data.DataStore, schema: Schema, n: Int) {
    try {
      store.createSchema(schema.toFeatureType)
      val tx = new org.geotools.data.DefaultTransaction
      try {
        val writer: org.geotools.data.FeatureWriter[org.opengis.feature.simple.SimpleFeatureType, org.opengis.feature.simple.SimpleFeature]
          = store.getFeatureWriter(schema.name, tx)
        try {
          val features = schema.features
          for (_ <- 0 until n) {
            val f = features.sample
            val toWrite = writer.next
            toWrite.setAttributes(f.get.getAttributes)
            writer.write
          } 
        } finally writer.close()
        tx.commit()
      } catch {
        case scala.util.control.NonFatal(e) => tx.rollback()
      } finally tx.close()
    } finally store.dispose()
  }
}

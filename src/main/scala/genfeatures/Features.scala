import org.scalacheck.{ Arbitrary, Gen }

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
}

package object genfeatures {
  implicit class ColumnSugar(name: String) extends AnyRef {
    def as[A : Arbitrary : Manifest]: Column[A] = Column(name)
  }

  def sequence[A](gens: Seq[Gen[A]]): Gen[Seq[A]] = {
    gens.foldLeft(Gen.const(Seq.empty[A])) { (gen, elem) => 
      for {
        g <- gen
        e <- elem
      } yield g :+ e
    }
  }
}

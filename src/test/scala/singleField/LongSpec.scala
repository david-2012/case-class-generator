

import com.julianpeeters.caseclass.generator._
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._ 
import scala.reflect.runtime.universe._




import org.specs2._
import mutable._
import specification._

class LongSpec extends mutable.Specification {

 
 "given a dynamically generated case class MyRecord_LongSpec(a: Long) as a type parameter, a grater" should {
    "serialize and deserialize correctly" in {


  val valueMembers: List[FieldData] = List(FieldData("a", typeOf[Long]))
  val classData = ClassData(ClassNamespace(Some("models")), ClassName("MyRecord_LongSpec"), ClassFieldData(valueMembers))
  val dcc = new DynamicCaseClass(classData)


  val typeTemplate = dcc.newInstance(1L)

  type MyRecord = typeTemplate.type


  val dbo = grater[MyRecord].asDBObject(typeTemplate)
    println(dbo)

  val obj = grater[MyRecord].asObject(dbo)
    println(obj)

      typeTemplate === obj
    }
}



}

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class ModelSpec extends Specification {

  import models._

  // -- Date helpers

  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  // --

  "Unit model" should {

    "be retrieved by id" in {
      running(FakeUnits(additionalConfiguration = inMemoryDatabase())) {

        val Some(macintosh) = Unit.findById(21)

        macintosh.name must equalTo("Macintosh")
        macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))

      }
    }

    "be listed along its companies" in {
      running(FakeUnits(additionalConfiguration = inMemoryDatabase())) {

        val units = Unit.list()

        units.total must equalTo(574)
        units.items must have length(10)

      }
    }

    "be updated if needed" in {
      running(FakeUnits(additionalConfiguration = inMemoryDatabase())) {

        Unit.update(21, Unit(name="The Macintosh", introduced=None, discontinued=None, companyId=Some(1)))

        val Some(macintosh) = Unit.findById(21)

        macintosh.name must equalTo("The Macintosh")
        macintosh.introduced must beNone

      }
    }

  }

}

package exercices

import java.net.URL

object ImplicitsTest extends App {

  object Implicits {
    implicit val myFunction1Context: List[String => String] = List(
      x => x.toLowerCase

    )

    implicit def stringToUrl(str: String): URL = new URL(str)

    implicit class StringToUrLable(str: String) {
      def toUrL: URL = new URL(str)
    }

  }

  def myImlicitParams(): Unit = {
    def _myFuntion(str: String)(implicit ctx: List[String => String]): String = {
      ctx.foldLeft(str)((acc, func) => func(acc))
    }
    import Implicits.myFunction1Context
    println(_myFuntion("  FOooo  "))
  }

  def myFunc2(): Unit = {
    def _myFunction(obj: URL): String = {
      obj.getHost
    }

    //    println(_myFunction(new URL("http://google.com")))

    import Implicits.stringToUrl
    println(_myFunction("http://google.com"))

    import Implicits.StringToUrLable
    println(_myFunction("http://google.com".toUrL))
  }
}

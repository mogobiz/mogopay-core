package imp

import org.apache.http.entity.StringEntity

/**
  * Created by hayssams on 13/03/16.
  */
object Run extends App {
  import java.io.File

  import org.apache.http.client.methods.HttpPost
  import org.apache.http.impl.client.DefaultHttpClient

  def post(url: String, data: String) {

    val post   = new HttpPost(url)
    val client = new DefaultHttpClient
    post.setEntity(new StringEntity(data))
    //    println(data)
    // send the post request
    val response = client.execute(post)
    //    response.getEntity.writeTo(System.out)
    //println(response.getStatusLine)
    if (response.getStatusLine.getStatusCode >= 400) {
      println(data)
      response.getEntity.writeTo(System.out)
      println(response.getStatusLine)
    }

  }
  def indexType(typ: String): Unit = {
    val dir   = new File(s"/Users/hayssams/tmp/i/mogopay/$typ")
    val files = dir.listFiles()
    files.filter(_.getName.contains('-')).foreach { file =>
      val id     = file.getName
      val source = scala.io.Source.fromFile(new File(file, "_source")).mkString
      post(s"http://elastic.ebiznext.com/mogopay/$typ/$id",
           source
             .replaceAll("é", "e")
             .replaceAll("ö", "o")
             .replaceAll("ü", "u")
             .replace("\"shipping\":null", "\"shippingInfo\":null"))
    }
  }
  indexType("BOTransactionLog")
}

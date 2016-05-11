import java.io.File
import scala.sys.process._

val dir = new File("/Users/hayssams/tmp/i/mogopay/Account")
val files = dir.listFiles()
files.filter(_.getName.contains('-')).map {
  file =>
    val id = file.getName
    val source = scala.io.Source.fromFile(new File(file, "_source")).mkString
    s"curl -XPUT 'http://localhost:12003/mogopay/Account/$id' -d '$source'"
} foreach {
  line =>
    val output = line.!!
    println(output)
}
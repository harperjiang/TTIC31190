package hw2

import scala.util.Random
import java.io.PrintWriter
import java.io.FileOutputStream
import scala.ref.SoftReference

object Main extends App {

  var sampleWords = List("people", "flew", "transported", "quickly", "good", "python", "apple", "red", "chicago", "language");

  def eval(title: String, out: PrintWriter, wordmatrix: WordMatrix) = {
    System.out.println(title);
    output.println(title);
    sampleWords.map(word => {
      System.out.println(word);
      out.println(word)
      wordmatrix.eval(word, 10).map(result => {
        System.out.println(result);
        out.println(result);
      });
    });

    var rlist = List[String]();
    rlist ++= wordmatrix.vwords()
    var random = new Random(System.currentTimeMillis());
    for (i <- 0 to 5) {
      var word = rlist(random.nextInt(rlist.size))
      System.out.println(word);
      out.println(word)
      wordmatrix.eval(word, 10).map(result => {
        out.println(result);
        System.out.println(result);
      });
    }
    out.flush();
  }

  var input = "res/wiki-2percent.txt"
  var output = new PrintWriter(new FileOutputStream("output"));
  /*
  var wc10k = new ArrayWordMatrix(4);
  wc10k.init("res/vocab-15k.txt", "res/vocab-10k.txt")
  wc10k.train(input)
  //eval("Result for 10K count", output, wc10k)
  var pmi10k = wc10k.pmi()
  eval("Result for 10K",output, pmi10k)
*/
  def wc3k() = {
    var wc3k = new ArrayWordMatrix(4);
    wc3k.init("res/vocab-15k.txt", "res/vocab-3k.txt")
    wc3k.train(input)
    var pmi3k = wc3k.pmi()
    eval("Result for 3K", output, pmi3k)
  }

  def wcr3k() = {
    var wcr3k = new ArrayWordMatrix(4);
    wcr3k.init("res/vocab-15k.txt", "res/vocab-rare3k.txt")
    wcr3k.train(input)
    var pmir3k = wcr3k.pmi()
    eval("Result for Rare 3K", output, pmir3k)
  }

  def wc1w() = {
    var wc1w = new ArrayWordMatrix(1);
    wc1w.init("res/vocab-15k.txt", "res/vocab-10k.txt")
    wc1w.train(input)
    var pmi1w = wc1w.pmi();
    eval("Result for Window 1", output, pmi1w)
  }

  def wc15w() = {
    var wc15w = new ArrayWordMatrix(15);
    wc15w.init("res/vocab-15k.txt", "res/vocab-10k.txt")
    wc15w.train(input)
    var pmi15w = wc15w.pmi();
    eval("Result for Window 15", output, pmi15w)
  }

  def wcnormal() = {
    var wcnormal = new ArrayWordMatrix(8);
    wcnormal.init("res/vocab-15k.txt", "res/vocab-10k.txt")
    wcnormal.train(input)
    var pminormal = wcnormal.pmi()
    eval("Result for normal", output, pminormal)
  }

  def wcweight() = {
    var wcweight = new CenterWordMatrix(8, i => { if (i > BigDecimal(8)) BigDecimal(0) else BigDecimal(8 - i) });
    wcweight.init("res/vocab-15k.txt", "res/vocab-10k.txt")
    wcweight.train(input)
    var pmiweight = wcweight.pmi()
    eval("Result for weight", output, pmiweight)
  }

  wc3k();
  wcr3k();
  wc1w();
  wc15w();
  wcnormal();
  wcweight();

  output.close();
}
package hw2

import scala.util.Random

object Main extends App {

  var sampleWords = List("people", "flew", "transported", "quickly", "good", "python", "apple", "red", "chicago", "language");

  def eval(title: String, wordmatrix: WordMatrix) = {
    System.out.println(title);
    sampleWords.map(word => {
      System.out.println(word);
      wordmatrix.eval(word, 10).map(result => {
        System.out.println(result);
      });
    });

    var rlist = List[String]();
    rlist ++= wordmatrix.vwords()
    var random = new Random(System.currentTimeMillis());
    for (i <- 0 to 5) {
      var word = rlist(random.nextInt(rlist.size))
      System.out.println(word);
      wordmatrix.eval(word, 10).map(result => {
        System.out.println(result);
      });
    }
  }
  var input = "res/wiki-2percent.txt"
/*
  var wc10k = new DefaultWordMatrix(4);
  wc10k.init("res/vocab-15k.txt", "res/vocab-10k.txt")
  wc10k.train(input)
  eval("Result for 10K count", wc10k)
  var pmi10k = wc10k.pmi()
  eval("Result for 10K", pmi10k)
*/
  var wc3k = new DefaultWordMatrix(4);
  wc3k.init("res/vocab-15k.txt", "res/vocab-3k.txt")
  wc3k.train(input)
  var pmi3k = wc3k.pmi()
  eval("Result for 3K", pmi3k)

  var wcr3k = new DefaultWordMatrix(4);
  wcr3k.init("res/vocab-15k.txt", "res/vocab-rare3k.txt")
  wcr3k.train(input)
  var pmir3k = wcr3k.pmi()
  eval("Result for Rare 3K", pmir3k)

  var wc1w = new DefaultWordMatrix(1);
  wc1w.init("res/vocab-15k.txt", "res/vocab-10k.txt")
  wc1w.train(input)
  var pmi1w = wc1w.pmi();
  eval("Result for Window 1", pmi1w)

  var wc15w = new DefaultWordMatrix(15);
  wc15w.init("res/vocab-15k.txt", "res/vocab-10k.txt")
  wc15w.train(input)
  var pmi15w = wc15w.pmi();
  eval("Result for Window 15", pmi15w)
}
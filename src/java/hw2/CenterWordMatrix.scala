package hw2

import scala.io.Source

class CenterWordMatrix(windowSize: Int, weight: (Int => Int)) extends DefaultWordMatrix(windowSize) {

  override def train(inputPath: String) = {
    Source.fromFile(inputPath).getLines().foreach(line => {
      var tokens = line.split("\\s");
      for (i <- 0 to tokens.length - 1) {
        var word = tokens(i);
        if (i < windowSize) {
          inc(word, startSymbol)(weight(i + 1));
        }
        for (j <- 0 to i - 1) {
          inc(word, tokens(j))(weight(i - j))
        }
        if (i >= tokens.length - windowSize) {
          inc(word, stopSymbol)(tokens.length - i);
        }
        for (j <- i + 1 to tokens.length - 1) {
          inc(word, tokens(j))(weight(j - i));
        }
      }
    });
  }
}
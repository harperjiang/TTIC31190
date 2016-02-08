package hw2

import scala.collection.mutable.PriorityQueue

trait WordMatrix {

  val startSymbol = "<s>";
  val stopSymbol = "</s>";

  def init(vpath: String, vcpath: String);
  def train(inputPath: String);
  def pmi(): WordMatrix;
  def eval(word: String, topn: Int): PriorityQueue[(BigDecimal, String)];
  def vwords(): scala.collection.Set[String];
}
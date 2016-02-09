package hw2

import scala.BigDecimal
import scala.Ordering
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.PriorityQueue
import scala.io.Source

class ArrayWordMatrix(windowSize: Int) extends WordMatrix {

  var rows = new HashMap[String, Int]();
  var columns = new HashMap[String, Int]();
  var array = Array.ofDim[BigDecimal](0, 0)

  def init(rowPath: String, columnPath: String) = {
    rows.clear
    columns.clear
    var counter = 0
    Source.fromFile(rowPath).getLines().foreach(line => {
      rows += line.trim() -> counter;
      counter += 1
    });

    counter = 0
    Source.fromFile(columnPath).getLines().foreach(line => {
      columns += (line.trim() -> counter);
      counter += 1
    });

    array = Array.fill[BigDecimal](rows.size, columns.size)(BigDecimal(0))
  }

  def train(inputPath: String) = {
    var counter = 0;
    Source.fromFile(inputPath).getLines().foreach(line => {
      counter += 1;
      if (counter % 10000 == 0)
        System.out.println(counter);
      var tokens = line.split("\\s");
      for (i <- 0 to tokens.length - 1) {
        var word = tokens(i);
        if (i < windowSize) {
          inc(word, startSymbol);
        }
        for (j <- 0 to i - 1) {
          inc(word, tokens(j))
        }
        if (i >= tokens.length - windowSize) {
          inc(word, stopSymbol);
        }
        for (j <- i + 1 to tokens.length - 1) {
          inc(word, tokens(j));
        }
      }
    });
  }

  def eval(refword: String, topn: Int): PriorityQueue[(BigDecimal, String)] = {
    var heap = PriorityQueue[(BigDecimal, String)]()(Ordering.by[(BigDecimal, String), BigDecimal](_._1)(Ordering[BigDecimal].reverse));

    this.rows.keySet.map(word => {
      if (!refword.equals(word)) {
        var item = (this.similarity(refword, word), word)
        if (item._1 != null) {
          heap.enqueue(item)
          while (heap.size >= topn) {
            heap.dequeue
          }
        }
      }
    });
    return heap;
  }

  def similarity(worda: String, wordb: String): BigDecimal = {

    var aIndex = rows.getOrElse(worda, -1)
    var bIndex = rows.getOrElse(wordb, -1)
    if (aIndex == -1 || bIndex == -1)
      return null;

    var dotprod = BigDecimal(0)
    var vasum2 = BigDecimal(0)
    var vbsum2 = BigDecimal(0)

    for (i <- 0 to columns.size - 1) {
      var va = array(aIndex)(i)
      var vb = array(bIndex)(i)
      dotprod += va * vb;
      vasum2 += va.pow(2)
      vbsum2 += vb.pow(2);
    }

    if (vasum2 == BigDecimal(0) || vbsum2 == BigDecimal(0))
      return null;
    return dotprod / BigDecimal(Math.sqrt((vasum2 * vbsum2).doubleValue))
  }

  def pmi(): WordMatrix = {
    var pmim = new ArrayWordMatrix(0);
    pmim.rows = this.rows;
    pmim.columns = this.columns;
    pmim.array = this.array.clone

    var denom = BigDecimal(0);

    for (i <- 0 to rows.size - 1) {
      for (j <- 0 to columns.size - 1) {
        denom += array(i)(j);
      }
    }

    var vBuffer = Array.ofDim[BigDecimal](rows.size);
    var vcBuffer = Array.fill[BigDecimal](columns.size)(BigDecimal(0));

    for (i <- 0 to rows.size - 1) {
      vBuffer(i) = array(i).sum
    }

    for (i <- 0 to columns.size - 1) {
      for (j <- 0 to rows.size - 1) {
        vcBuffer(i) += array(j)(i)
      }
    }

    this.rows.map(vkey => {
      var vword = vkey._1;
      var vIndex = vkey._2;
      var vValue = vBuffer(vIndex);

      this.columns.map(vckey => {
        var vcword = vckey._1
        var vcIndex = vckey._2
        var vcValue = vcBuffer(vcIndex);

        var pmivalue = BigDecimal(0)
        if (array(vIndex)(vcIndex) != BigDecimal(0) && vValue != BigDecimal(0) && vcValue != BigDecimal(0)) {
          pmivalue = BigDecimal(Math.log((array(vIndex)(vcIndex) * denom).doubleValue) - Math.log((vValue * vcValue).doubleValue));
          if (pmivalue < BigDecimal(0))
            pmivalue = BigDecimal(0);
        }
        pmim.set(vword, vcword, pmivalue);
      })
    });
    return pmim;
  }

  def vwords(): scala.collection.Set[String] = {
    return rows.keySet
  }

  def inc(vWord: String, vcWord: String)(implicit value: BigDecimal = BigDecimal(1)): Unit = {
    var rowIndex = rows.getOrElse(vWord, -1)
    var colIndex = columns.getOrElse(vcWord, -1)
    if (rowIndex == -1 || colIndex == -1)
      return
    array(rowIndex)(colIndex) += value
  }

  def set(vWord: String, vcWord: String, value: BigDecimal): Unit = {
    var rowIndex = rows.getOrElse(vWord, -1)
    var colIndex = columns.getOrElse(vcWord, -1)
    if (rowIndex == -1 || colIndex == -1)
      return
    array(rowIndex)(colIndex) = value
  }
}
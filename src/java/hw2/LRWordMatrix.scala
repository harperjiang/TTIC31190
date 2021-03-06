package hw2

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.io.Source
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.Queue
import java.util.Collections

class LRWordMatrix(windowSize: Int) extends WordMatrix {

  var rows = new HashMap[String, Int]();
  var columns = new HashMap[String, Int]();
  var array = Array.ofDim[(BigDecimal, BigDecimal)](0, 0)

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

    array = Array.fill[(BigDecimal, BigDecimal)](rows.size, columns.size)((BigDecimal(0), BigDecimal(0)))
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
          linc(word, startSymbol);
        }
        for (j <- 0 to i - 1) {
          linc(word, tokens(j))
        }
        if (i >= tokens.length - windowSize) {
          rinc(word, stopSymbol);
        }
        for (j <- i + 1 to tokens.length - 1) {
          rinc(word, tokens(j));
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
      dotprod += va._1 * vb._1 + va._2 + vb._2;
      vasum2 += va._1.pow(2) + va._2.pow(2)
      vbsum2 += vb._1.pow(2) + vb._2.pow(2);
    }

    if (vasum2 == BigDecimal(0) || vbsum2 == BigDecimal(0))
      return null;
    return dotprod / BigDecimal(Math.sqrt((vasum2 * vbsum2).doubleValue))
  }

  def pmi(): WordMatrix = {
    var pmim = new LRWordMatrix(0);
    pmim.rows = this.rows;
    pmim.columns = this.columns;
    pmim.array = this.array.clone

    var denom = BigDecimal(0);

    for (i <- 0 to rows.size - 1) {
      for (j <- 0 to columns.size - 1) {
        denom += array(i)(j)._1 + array(i)(j)._2;
      }
    }

    var vBuffer = Array.ofDim[BigDecimal](rows.size);
    var vcBuffer1 = Array.fill[BigDecimal](columns.size)(BigDecimal(0));
    var vcBuffer2 = Array.fill[BigDecimal](columns.size)(BigDecimal(0));

    for (i <- 0 to rows.size - 1) {
      vBuffer(i) = array(i).map(s => s._1 + s._2).sum
    }

    for (i <- 0 to columns.size - 1) {
      for (j <- 0 to rows.size - 1) {
        vcBuffer1(i) += array(j)(i)._1
        vcBuffer2(i) += array(j)(i)._2
      }
    }

    this.rows.map(vkey => {
      var vword = vkey._1;
      var vIndex = vkey._2;
      var vValue = vBuffer(vIndex);

      this.columns.map(vckey => {
        var vcword = vckey._1
        var vcIndex = vckey._2
        var vcValue1 = vcBuffer1(vcIndex);
        var vcValue2 = vcBuffer2(vcIndex);

        var pmivalue1 = BigDecimal(0)
        if (vckey._2 != BigDecimal(0) && vValue != BigDecimal(0) && vcValue1 != BigDecimal(0)) {
          pmivalue1 = BigDecimal(Math.log((vckey._2 * denom).doubleValue) - Math.log((vValue * vcValue1).doubleValue));
          if (pmivalue1 < BigDecimal(0))
            pmivalue1 = BigDecimal(0);
        }

        var pmivalue2 = BigDecimal(0)
        if (vckey._2 != BigDecimal(0) && vValue != BigDecimal(0) && vcValue2 != BigDecimal(0)) {
          pmivalue2 = BigDecimal(Math.log((vckey._2 * denom).doubleValue) - Math.log((vValue * vcValue2).doubleValue));
          if (pmivalue2 < BigDecimal(0))
            pmivalue2 = BigDecimal(0);
        }

        pmim.set(vword, vcword, (pmivalue1, pmivalue2));
      })
    });
    return pmim;
  }

  def vwords(): scala.collection.Set[String] = {
    return rows.keySet
  }

  def linc(vWord: String, vcWord: String)(implicit value: BigDecimal = BigDecimal(1)): Unit = {
    var rowIndex = rows.getOrElse(vWord, -1)
    var colIndex = columns.getOrElse(vcWord, -1)
    if (rowIndex == -1 || colIndex == -1)
      return
    var oldval = array(rowIndex)(colIndex)
    array(rowIndex)(colIndex) = (oldval._1 + value, oldval._2)
  }

  def rinc(vWord: String, vcWord: String)(implicit value: BigDecimal = BigDecimal(1)): Unit = {
    var rowIndex = rows.getOrElse(vWord, -1)
    var colIndex = columns.getOrElse(vcWord, -1)
    if (rowIndex == -1 || colIndex == -1)
      return
    var oldval = array(rowIndex)(colIndex)
    array(rowIndex)(colIndex) = (oldval._1, oldval._2 + value)
  }

  def set(vWord: String, vcWord: String, value: (BigDecimal, BigDecimal)): Unit = {
    var rowIndex = rows.getOrElse(vWord, -1)
    var colIndex = columns.getOrElse(vcWord, -1)
    if (rowIndex == -1 || colIndex == -1)
      return
    array(rowIndex)(colIndex) = value
  }
}
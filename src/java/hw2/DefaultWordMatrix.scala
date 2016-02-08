package hw2

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.io.Source
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.Queue
import java.util.Collections

class DefaultWordMatrix(windowSize: Int) extends WordMatrix {

  var matrix = new HashMap[String, HashMap[String, BigDecimal]]();

  var columns = new HashSet[String]();

  def init(rowPath: String, columnPath: String) = {
    matrix.clear
    columns.clear
    Source.fromFile(rowPath).getLines().foreach(line => {
      matrix += (line.trim() -> new HashMap[String, BigDecimal]());
    });

    Source.fromFile(columnPath).getLines().foreach(line => {
      columns += (line.trim());
    });
  }

  def train(inputPath: String) = {
    Source.fromFile(inputPath).getLines().foreach(line => {
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
    this.matrix.keySet.map(word => {
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
    if (matrix(worda) == null || matrix(worda).isEmpty || matrix(wordb) == null || matrix(wordb).isEmpty)
      return null;

    var words = matrix(worda).keySet.union(matrix(wordb).keySet)

    var va = matrix(worda);
    var vb = matrix(wordb);
    var dotprod = BigDecimal(0)
    var vasum2 = BigDecimal(0)
    var vbsum2 = BigDecimal(0)

    words.map(word => {
      var vacnt = va.getOrElse(word, BigDecimal(0))
      var vbcnt = vb.getOrElse(word, BigDecimal(0))
      dotprod += vacnt * vbcnt;
      vasum2 += vacnt.pow(2)
      vbsum2 += vbcnt.pow(2);
    });
    if (vasum2.equals(BigDecimal(0)) || vbsum2.equals(BigDecimal(0)))
      return null;
    return dotprod / BigDecimal(Math.sqrt((vasum2 * vbsum2).doubleValue))
  }

  def pmi(): WordMatrix = {
    var pmim = new DefaultWordMatrix(0);
    var denom = BigDecimal(0);

    this.matrix.map(kv => {
      kv._2.map(kv2 => {
        denom += kv2._2;
      })
    });

    var vBuffer = new HashMap[String, BigDecimal]();
    var vcBuffer = new HashMap[String, BigDecimal]();

    this.matrix.map(vkey => {
      var vword = vkey._1;

      vkey._2.map(vckey => {
        var vcword = vckey._1

        var vValue = vBuffer.getOrElseUpdate(vword, {
          vkey._2.map(_._2).sum
        });

        var vcValue = vcBuffer.getOrElseUpdate(vcword, {
          matrix.map(_._2.getOrElse(vcword, BigDecimal(0))).sum
        });

        var pmivalue = BigDecimal(0)
        if (vckey._2 != BigDecimal(0) && vValue != BigDecimal(0) && vcValue != BigDecimal(0)) {
          pmivalue = BigDecimal(Math.log((vckey._2 * denom).doubleValue) - Math.log((vValue * vcValue).doubleValue));
          if (pmivalue < BigDecimal(0))
            pmivalue = BigDecimal(0);
        }
        pmim.set(vword, vcword, pmivalue);
      })
    });
    return pmim;
  }

  def vwords(): scala.collection.Set[String] = {
    return matrix.keySet
  }

  def inc(vWord: String, vcWord: String)(implicit value: BigDecimal = BigDecimal(1)) = {
    if (matrix.contains(vWord)) {
      var currentValue = matrix(vWord).getOrElse(vcWord, BigDecimal(0))
      currentValue += value
      matrix(vWord) += (vcWord -> currentValue);
    }
  }

  def set(vWord: String, vcWord: String, value: BigDecimal) = {
    matrix.getOrElseUpdate(vWord, new HashMap[String, BigDecimal]).put(vcWord, value);
  }
}
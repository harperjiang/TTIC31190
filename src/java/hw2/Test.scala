package hw2

import scala.collection.mutable.PriorityQueue

object Test extends App {

  var queue = new PriorityQueue[(Int,String)]()(Ordering.by[(Int,String),Int](_._1)(Ordering[Int].reverse));
  queue += ((1,"x"),(2,"y"),(3,"z"),(4,"w"),(5,"j"),(6,"k"),(7,"m"),(8,"w"),(9,"t"));
  System.out.println(queue.dequeue)
}
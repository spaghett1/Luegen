package de.htwg.luegen.Controller


trait Observable {
  def registerObserver(o: Observer): Unit
  def notifyObservers(): Unit
}
  
trait Observer {
  def updateDisplay(): Unit
}
 
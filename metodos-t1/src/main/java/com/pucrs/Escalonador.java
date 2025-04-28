package com.pucrs;

import java.util.PriorityQueue;

public class Escalonador {

  private PriorityQueue<Evento> eventQueue;

  public Escalonador() {
    this.eventQueue = new PriorityQueue<>();
  }

  public void addEvent(Evento evento) {
    eventQueue.add(evento);
  }

  public Evento nextEvent() {
    return eventQueue.poll();
  }

  public boolean hasEvents() {
    return !eventQueue.isEmpty();
  }

  public int eventQuantity() {
    return eventQueue.size();
  }
}

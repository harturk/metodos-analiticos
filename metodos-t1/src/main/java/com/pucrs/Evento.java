package com.pucrs;

public class Evento implements Comparable<Evento> {
  private final double time;
  private final int source;
  private final int target;
  private final int type;

  public Evento(double time, int source, int target, int type) {
    this.time = time;
    this.source = source;
    this.target = target;
    this.type = type;
  }

  public double getTime() {
    return time;
  }

  public int getType() {
    return type;
  }

  public int getSource() {
    return source;
  }

  public int getTarget() {
    return target;
  }

  @Override
  public int compareTo(Evento otherEvent) {
    return Double.compare(this.time, otherEvent.time);
  }
}

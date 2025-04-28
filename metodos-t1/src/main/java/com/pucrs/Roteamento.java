package com.pucrs;

public class Roteamento {
  private final int target;
  private final double probability;

  public Roteamento(int target, double probability) {
    this.target = target;
    this.probability = probability;
  }

  public int getTarget() {
    return target;
  }

  public double getProbability() {
    return probability;
  }
}

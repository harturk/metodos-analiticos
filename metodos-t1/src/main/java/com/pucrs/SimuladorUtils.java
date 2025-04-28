package com.pucrs;

import java.util.Random;

public class SimuladorUtils {

  private final Random generator;
  private final int maxRandomNum;
  private int used;

  public SimuladorUtils(int maxRandomNum) {
    this.generator = new Random();
    this.maxRandomNum = maxRandomNum;
    this.used = 0;
  }

  public double uniform(double min, double max) {
    if (used >= maxRandomNum) {
      throw new RuntimeException("Limite de números aleatórios atingido.");
    }
    used++;
    return min + (max - min) * generator.nextDouble();
  }

  public double uniform01() {
    if (used >= maxRandomNum) {
      throw new RuntimeException("Limite de números aleatórios atingido.");
    }
    used++;
    return generator.nextDouble();
  }

  public int getUsed() {
    return used;
  }

  public boolean limitReached() {
    return used >= maxRandomNum;
  }
}

package com.pucrs;

import java.util.Random;

public class SimuladorUtils {

  private final Random generator;
  private final int maxRandomNum;
  private int used;

  public SimuladorUtils(int maxRandomNum, int seed) {
    this.generator = new Random(seed);
    this.maxRandomNum = maxRandomNum;
    this.used = 0;
  }

  public double uniform(double min, double max) {
    return min + (max - min) * uniform01();
  }

  public double uniform01() {
    if (used >= maxRandomNum)
      throw new RuntimeException("Limite de números aleatórios atingido.");

    double val = generator.nextDouble();
    used++;
    return val;
  }

  public int getUsed() {
    return used;
  }

  public boolean limitReached() {
    return used >= maxRandomNum;
  }
}

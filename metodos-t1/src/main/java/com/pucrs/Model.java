package com.pucrs;

import java.util.List;
import java.util.Map;

public class Model {
  public Map<String, Double> arrivals;
  public Map<String, QueueDef> queues;
  public List<Route> network;
  public int rndnumbersPerSeed;
  public List<Integer> seeds;

  public static class QueueDef {
    public int servers;
    public String capacity;
    public Double minArrival;
    public Double maxArrival;
    public double minService;
    public double maxService;
  }

  public static class Route {
    public String source;
    public String target;
    public double probability;
  }
}

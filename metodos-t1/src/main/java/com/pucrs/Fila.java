package com.pucrs;

import java.util.ArrayList;
import java.util.List;

public class Fila {
  private String name;
  private int servers, capacity, customers, loss;
  private Double minArrival, maxArrival, minService, maxService, lastUpdate;
  private double[] times;
  private final List<Roteamento> routings;

  public Fila(String name, int servers, int capacity, Double minArrival, Double maxArrival, double minService,
      double maxService) {
    this.name = name;
    this.servers = servers;
    this.capacity = capacity;
    this.minArrival = minArrival;
    this.maxArrival = maxArrival;
    this.minService = minService;
    this.maxService = maxService;
    this.customers = 0;
    this.loss = 0;
    this.times = new double[capacity + 1];
    this.lastUpdate = 0.0;
    this.routings = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public int capacity() {
    return capacity;
  }

  public int servers() {
    return servers;
  }

  public int loss() {
    return loss;
  }

  public Double getMinArrival() {
    return minArrival;
  }

  public Double getMaxArrival() {
    return maxArrival;
  }

  public Double getMinService() {
    return minService;
  }

  public Double getMaxService() {
    return maxService;
  }

  public int getCustomers() {
    return customers;
  }

  public List<Roteamento> getRoutings() {
    return routings;
  }

  public void addRouting(Roteamento r) {
    routings.add(r);
  }

  public void updateTime(double now) {
    double delta = now - lastUpdate;
    if (customers >= 0 && customers <= capacity) {
      times[customers] += delta;
    }
    lastUpdate = now;
  }

  public void incrementLoss() {
    loss++;
  }

  public void incrementCustomers() {
    customers++;
  }

  public void decrementCustomers() {
    if (customers > 0) {
      customers--;
    }
  }

  public double[] getTimes() {
    return times;
  }
}

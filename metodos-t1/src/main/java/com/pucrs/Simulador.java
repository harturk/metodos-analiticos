package com.pucrs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Simulador {

  private List<Fila> queues;
  private Map<String, Integer> queueMap;
  private Escalonador scheduler;
  private SimuladorUtils utils;
  private Model model;

  public void execute(String yamlFile) throws Exception {
    loadModel(yamlFile);
    initializeQueues();
    initializeFirstEvents();

    double tempoAtual = 0.0;

    while (scheduler.hasEvents() && !utils.limitReached()) {
      Evento evento = scheduler.nextEvent();
      double novoTempo = evento.getTime();

      for (Fila queue : queues) {
        queue.updateTime(novoTempo);
      }

      tempoAtual = novoTempo;

      if (evento.getType() == 0) {
        handleArrival(evento, tempoAtual);
      } else {
        handleExit(evento, tempoAtual);
      }
    }

    showResults(tempoAtual);
  }

  private void loadModel(String yamlFile) throws Exception {
    LoaderOptions options = new LoaderOptions();
    Constructor constructor = new Constructor(Model.class, options);
    Yaml yaml = new Yaml(constructor);

    try (InputStream in = new FileInputStream(yamlFile)) {
      model = yaml.load(in);
    }
  }

  private void initializeQueues() {
    queues = new ArrayList<>();
    queueMap = new HashMap<>();
    scheduler = new Escalonador();
    utils = new SimuladorUtils(model.rndnumbersPerSeed, model.seeds.get(0));

    int idx = 0;
    for (Map.Entry<String, Model.QueueDef> entry : model.queues.entrySet()) {
      Model.QueueDef def = entry.getValue();

      int capacity;

      if (def.capacity.equalsIgnoreCase("INF")) {
        capacity = Integer.MAX_VALUE;
      } else {
        try {
          capacity = Integer.parseInt(def.capacity);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid capacity: " + def.capacity);
        }
      }

      Fila queue = new Fila(
          entry.getKey(),
          def.servers,
          capacity,
          def.minArrival,
          def.maxArrival,
          def.minService,
          def.maxService);
      queues.add(queue);
      queueMap.put(entry.getKey(), idx++);
    }

    for (Model.Route r : model.network) {
      int source = queueMap.get(r.source);
      int target = r.target.equals("EXIT") ? -1 : queueMap.get(r.target);
      queues.get(source).addRouting(new Roteamento(target, r.probability));
    }
  }

  private void initializeFirstEvents() {
    if (model.arrivals != null) {
      for (Map.Entry<String, Double> entry : model.arrivals.entrySet()) {
        int target = queueMap.get(entry.getKey());
        scheduler.addEvent(new Evento(entry.getValue(), -1, target, 0));
      }
    }
  }

  private void handleArrival(Evento event, double currentTime) {
    Fila queue = queues.get(event.getTarget());

    if (queue.getCustomers() >= queue.capacity()) {
      queue.incrementLoss();
    } else {
      queue.incrementCustomers();

      if (queue.getCustomers() <= queue.servers()) {
        double serviceTime = utils.uniform(queue.getMinService(), queue.getMaxService());
        scheduler.addEvent(new Evento(currentTime + serviceTime, event.getTarget(), event.getTarget(), 1));
      }
    }

    if (event.getSource() == -1 && queue.getMinArrival() != null) {
      double interArrival = utils.uniform(queue.getMinArrival(), queue.getMaxArrival());
      scheduler.addEvent(new Evento(currentTime + interArrival, -1, event.getTarget(), 0));
    }
  }

  private void handleExit(Evento event, double currentTime) {
    Fila sourceQueue = queues.get(event.getSource());

    sourceQueue.decrementCustomers();

    if (sourceQueue.getCustomers() >= sourceQueue.servers()) {
      double serviceTime = utils.uniform(sourceQueue.getMinService(), sourceQueue.getMaxService());
      scheduler.addEvent(new Evento(currentTime + serviceTime, event.getSource(), event.getTarget(), 1));
    }

    double u = utils.uniform01();
    double acc = 0.0;

    for (Roteamento r : sourceQueue.getRoutings()) {
      acc += r.getProbability();
      if (u <= acc) {
        if (r.getTarget() != -1) {
          scheduler.addEvent(new Evento(currentTime, event.getSource(), r.getTarget(), 0));
        }
        break;
      }
    }
  }

  private void showResults(double simulationTime) {
    System.out.printf("=== Fim da simulação (tempo %.4f) ===\n\n", simulationTime);
    for (Fila queue : queues) {
      System.out.println("*********************************************************");

      String header = String.format("Queue:   %s (G/G/%d%s)",
          queue.getName(),
          queue.servers(),
          queue.capacity() == Integer.MAX_VALUE ? "" : ("/" + queue.capacity()));
      System.out.println(header);

      if (queue.getMinArrival() != null) {
        System.out.printf("Arrival: %.1f ... %.1f\n", queue.getMinArrival(), queue.getMaxArrival());
      }

      System.out.printf("Service: %.1f ... %.1f\n", queue.getMinService(), queue.getMaxService());

      System.out.println("*********************************************************");
      System.out.printf("%6s %20s %22s\n", "State", "Time", "Probability");

      double[] times = queue.getTimes();
      for (int i = 0; i < times.length; i++) {
        if (times[i] > 0.0) {
          double percent = (simulationTime > 0) ? (times[i] / simulationTime) * 100 : 0;
          System.out.printf("%6d %20.4f %21.2f%%\n", i, times[i], percent);
        }
      }

      System.out.printf("\nNumber of losses: %d\n\n", queue.loss());
    }

    System.out.println("=========================================================");
    System.out.printf("Simulation average time: %.4f\n", simulationTime);
    System.out.println("=========================================================");
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Uso: java SimuladorRedeFilas <config.yaml>");
      System.exit(1);
    }
    new Simulador().execute(args[0]);
  }
}

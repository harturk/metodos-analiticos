package com.pucrs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class SimuladorRedeFilasYAML {

    // === Modelos de dados para desserializar o YAML ===

    public static class Model {
        public Map<String, Double> arrivals;
        public Map<String, QueueDef> queues;
        public List<Route> network;
        public int rndnumbersPerSeed;
        public List<Integer> seeds;
    }

    public static class QueueDef {
        public int servers;
        public int capacity;
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

    // === Classes internas de simulação ===

    class Fila {
        String name;
        int servers, capacity;
        Double minArr, maxArr; // nulos se não tiver chegadas externas
        double minSvc, maxSvc;
        double[] tempoEstado;
        double lastUpdate;
        int inService = 0, inQueue = 0;
        int lost = 0;

        Map<Fila, Double> routing = new LinkedHashMap<>(); // probabilidades para outras filas
        double routingExitProb = 0.0;

        Fila(String name, QueueDef def, Double arrivalInstant) {
            this.name = name;
            this.servers = def.servers;
            this.capacity = def.capacity;
            this.minArr = def.minArrival;
            this.maxArr = def.maxArrival;
            this.minSvc = def.minService;
            this.maxSvc = def.maxService;
            this.tempoEstado = new double[def.capacity + 1];
            this.lastUpdate = (arrivalInstant != null ? arrivalInstant : 0.0);
            // arrivals map guarda o instante do primeiro cliente, mas não é
            // parte de QueueDef: o scheduling inicial é feito à parte
        }

        void updateStats(double now) {
            double dt = now - lastUpdate;
            int state = inService + inQueue;
            if (state <= capacity)
                tempoEstado[state] += dt;
            lastUpdate = now;
        }
    }

    class Evento implements Comparable<Evento> {
        double time;
        int type; // 0=chegada, 1=saída
        Fila fila;

        Evento(double t, int type, Fila f) {
            this.time = t;
            this.type = type;
            this.fila = f;
        }

        @Override
        public int compareTo(Evento o) {
            return Double.compare(this.time, o.time);
        }
    }

    // === Variáveis do simulador ===

    Model model;
    Map<String, Fila> filas = new LinkedHashMap<>();
    PriorityQueue<Evento> evQueue = new PriorityQueue<>();
    Random rnd;
    int rndCount = 0;

    // === Leitura do YAML ===

    private void loadModel(String yamlFile) throws Exception {
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(Model.class, options);
        Yaml yaml = new Yaml(constructor);
        try (InputStream in = new FileInputStream(yamlFile)) {
            model = yaml.load(in);
        }
        // monta as filas
        model.queues.forEach((name, def) -> {
            Double firstArr = model.arrivals != null ? model.arrivals.get(name) : null;
            filas.put(name, new Fila(name, def, firstArr));
        });
        // monta roteamento
        for (Route r : model.network) {
            Fila src = filas.get(r.source);
            if (r.target.equals("EXIT")) {
                src.routingExitProb += r.probability;
            } else {
                src.routing.put(filas.get(r.target), r.probability);
            }
        }
        // prepara Random
        rnd = new Random();
        rndCount = 0;
    }

    // gera U~Uniforme[min,max], ou retorna -1 para parar
    private double nextRnd(double min, double max) {
        if (rndCount >= model.rndnumbersPerSeed)
            return -1;
        rndCount++;
        return min + (max - min) * rnd.nextDouble();
    }

    // === Inicialização e loop principal ===

    public void run(String yamlFile) throws Exception {
        loadModel(yamlFile);

        double now = 0.0;
        // agenda chegadas externas iniciais
        for (Map.Entry<String, Double> e : model.arrivals.entrySet()) {
            Fila f = filas.get(e.getKey());
            now = e.getValue();
            f.lastUpdate = now;
            evQueue.add(new Evento(now, 0, f));
        }

        while (!evQueue.isEmpty()) {
            Evento ev = evQueue.poll();
            // atualiza estatísticas de TODAS as filas até ev.time
            filas.values().forEach(f -> f.updateStats(ev.time));
            now = ev.time;
            if (rndCount >= model.rndnumbersPerSeed)
                break;

            if (ev.type == 0)
                processArrival(ev, now);
            else
                /* tipo==1 */ processDeparture(ev, now);
        }

        // === Impressão dos resultados ===
        System.out.printf("=== Simulação encerrada: t=%.4f, rnds usados=%d ===%n", now, rndCount);
        for (Fila f : filas.values()) {
            System.out.println();
            System.out.println("Fila " + f.name + ":");
            System.out.println("  Clientes perdidos = " + f.lost);
            System.out.println("  Tempo acumulado por estado:");
            for (int i = 0; i < f.tempoEstado.length; i++) {
                double pct = now > 0 ? (f.tempoEstado[i] / now) * 100 : 0;
                System.out.printf("    [%2d] tempo=%.4f  prob=%.2f%%%n", i, f.tempoEstado[i], pct);
            }
        }
    }

    // Chegada externa ou interna
    private void processArrival(Evento ev, double now) {
        Fila f = ev.fila;
        int inSys = f.inService + f.inQueue;
        if (inSys >= f.capacity) {
            f.lost++;
        } else {
            // cliente entra
            if (f.inService < f.servers) {
                // inicia serviço imediatamente
                double s = nextRnd(f.minSvc, f.maxSvc);
                if (s < 0)
                    return;
                evQueue.add(new Evento(now + s, 1, f));
                f.inService++;
            } else {
                // espera na fila
                f.inQueue++;
            }
        }
        // agendar próxima chegada EXTERNA, se aplicável
        if (f.minArr != null) {
            double iat = nextRnd(f.minArr, f.maxArr);
            if (iat < 0)
                return;
            evQueue.add(new Evento(now + iat, 0, f));
        }
    }

    // Cliente termina serviço em f e faz roteamento
    private void processDeparture(Evento ev, double now) {
        Fila f = ev.fila;
        // liberar servidor
        f.inService--;
        // se havia fila de espera:
        if (f.inQueue > 0) {
            f.inQueue--;
            double s = nextRnd(f.minSvc, f.maxSvc);
            if (s >= 0) {
                evQueue.add(new Evento(now + s, 1, f));
                f.inService++;
            }
        }
        // roteamento
        double u = rnd.nextDouble();
        rndCount++;
        double acc = 0.0;
        for (Map.Entry<Fila, Double> e : f.routing.entrySet()) {
            acc += e.getValue();
            if (u <= acc) {
                evQueue.add(new Evento(now, 0, e.getKey()));
                return;
            }
        }
        // saída do sistema
        // (caso haja prob saída explicitada, basta não agendar nada aqui)
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: java SimuladorRedeFilasYAML <config.yaml>");
            System.exit(1);
        }
        new SimuladorRedeFilasYAML().run(args[0]);
    }
}
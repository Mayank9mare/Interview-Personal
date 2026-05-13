import java.util.*;

public class CurrencyArbitrage {

    // ═══════════════════════════════════════════════════════════════════════════
    // Currency Arbitrage Detection
    //
    // Problem: Given a set of currency exchange rates, determine if there exists
    // a sequence of exchanges that starts and ends at the same currency and
    // yields a net profit (i.e. the product of rates around the cycle > 1).
    //
    // Example (arbitrage):
    //   USD → EUR (×0.9)  →  EUR → GBP (×0.8)  →  GBP → USD (×1.5)
    //   Net: 0.9 × 0.8 × 1.5 = 1.08  >  1.0  →  8% profit per cycle
    //
    // Key insight — log transformation:
    //   Product > 1 ↔ sum of logs > 0 ↔ sum of NEGATIVE logs < 0
    //   So assign edge weight = -ln(rate).
    //   Arbitrage cycle ↔ negative-weight cycle in this log graph.
    //
    // Algorithm: Bellman-Ford
    //   Initialise dist[all] = 0 (equivalent to a super-source with 0-weight
    //   edges to every node, so cycles anywhere are reachable).
    //   Run V relaxations. Any node relaxed in the V-th round is in or reachable
    //   from a negative cycle.
    //   To extract the cycle: back-track through predecessor pointers.
    //
    // Complexity:
    //   Time  O(V · E)  V = #currencies, E = #exchange-rate pairs
    //   Space O(V + E)
    //
    // addRate(from, to, rate) — add a directed exchange rate
    // hasArbitrage()          — boolean
    // getArbitragePath()      — the profitable cycle, e.g. [USD, EUR, GBP, USD]
    // ═══════════════════════════════════════════════════════════════════════════

    static class ArbitrageDetector {
        private static final double EPS = 1e-10;

        private final Map<String, Integer> index = new HashMap<>();  // currency → node id
        private final List<String> currencies    = new ArrayList<>();
        // Each edge: [from-index, to-index, -ln(rate)]
        private final List<double[]> edges         = new ArrayList<>();
        private final List<String[]> edgeCurrencies = new ArrayList<>(); // for display

        public void addRate(String from, String to, double rate) {
            intern(from);
            intern(to);
            edges.add(new double[]{ index.get(from), index.get(to), -Math.log(rate) });
            edgeCurrencies.add(new String[]{ from, to });
        }

        private void intern(String c) {
            if (!index.containsKey(c)) {
                index.put(c, currencies.size());
                currencies.add(c);
            }
        }

        public boolean hasArbitrage() {
            int V = currencies.size();
            double[] dist = new double[V]; // all 0 = super-source init

            // V-1 relaxations
            for (int i = 0; i < V - 1; i++) relax(dist, null);

            // V-th pass: if any edge still relaxable → negative cycle
            double[] copy = dist.clone();
            relax(copy, null);
            return !Arrays.equals(dist, copy);
        }

        // Returns the arbitrage cycle or empty list if none.
        // Example: ["USD", "EUR", "GBP", "USD"]
        public List<String> getArbitragePath() {
            int V = currencies.size();
            double[] dist = new double[V];
            int[] pred    = new int[V];
            Arrays.fill(pred, -1);

            int lastRelaxed = -1;
            for (int i = 0; i < V; i++) {
                lastRelaxed = -1;
                lastRelaxed = relax(dist, pred);
            }

            if (lastRelaxed == -1) return Collections.emptyList();

            // Walk back V steps from lastRelaxed to land inside the cycle
            int inCycle = lastRelaxed;
            for (int i = 0; i < V; i++) inCycle = pred[inCycle];

            // Trace the cycle
            List<String> cycle = new ArrayList<>();
            int curr = inCycle;
            Set<Integer> seen = new LinkedHashSet<>();
            while (seen.add(curr)) {
                cycle.add(currencies.get(curr));
                curr = pred[curr];
            }
            cycle.add(currencies.get(curr)); // close: repeat start node
            Collections.reverse(cycle);
            return cycle;
        }

        // One full Bellman-Ford relaxation pass; returns last node relaxed (-1 if none)
        private int relax(double[] dist, int[] pred) {
            int lastRelaxed = -1;
            for (double[] e : edges) {
                int u = (int) e[0], v = (int) e[1];
                double w = e[2];
                if (dist[u] + w < dist[v] - EPS) {
                    dist[v] = dist[u] + w;
                    if (pred != null) pred[v] = u;
                    lastRelaxed = v;
                }
            }
            return lastRelaxed;
        }

        public double cycleProfit(List<String> path) {
            // Walk the path and multiply rates
            Map<String, Map<String, Double>> rateMap = new HashMap<>();
            for (int i = 0; i < edges.size(); i++) {
                String f = edgeCurrencies.get(i)[0], t = edgeCurrencies.get(i)[1];
                double rate = Math.exp(-edges.get(i)[2]);
                rateMap.computeIfAbsent(f, k -> new HashMap<>()).put(t, rate);
            }
            double product = 1.0;
            for (int i = 0; i < path.size() - 1; i++) {
                String f = path.get(i), t = path.get(i + 1);
                double rate = rateMap.getOrDefault(f, Collections.emptyMap())
                                     .getOrDefault(t, 1.0);
                product *= rate;
                System.out.printf("  %s → %s  ×%.4f  (running: %.6f)%n", f, t, rate, product);
            }
            return product;
        }

        public void printRates() {
            System.out.println("Exchange rates:");
            for (int i = 0; i < edges.size(); i++) {
                String[] ec = edgeCurrencies.get(i);
                System.out.printf("  %-5s → %-5s  %.4f%n",
                                  ec[0], ec[1], Math.exp(-edges.get(i)[2]));
            }
        }
    }

    public static void main(String[] args) {

        // ── 1. Arbitrage exists ───────────────────────────────────────────────
        System.out.println("══════════════════════════════════════════");
        System.out.println(" Example 1: Arbitrage (3-currency cycle) ");
        System.out.println("══════════════════════════════════════════");
        ArbitrageDetector d1 = new ArbitrageDetector();
        d1.addRate("USD", "EUR", 0.9);
        d1.addRate("EUR", "GBP", 0.8);
        d1.addRate("GBP", "USD", 1.5);   // 0.9×0.8×1.5 = 1.08 → profit
        // Add reverse rates (realistic market)
        d1.addRate("EUR", "USD", 1.11);
        d1.addRate("GBP", "EUR", 1.25);
        d1.addRate("USD", "GBP", 0.74);
        d1.printRates();
        System.out.println("Has arbitrage: " + d1.hasArbitrage());
        List<String> path1 = d1.getArbitragePath();
        System.out.println("Arbitrage path: " + path1);
        if (!path1.isEmpty()) {
            System.out.println("Profit trace:");
            double profit = d1.cycleProfit(path1);
            System.out.printf("Net multiplier: %.6f  (%.2f%% gain)%n", profit, (profit - 1) * 100);
        }

        // ── 2. No arbitrage ───────────────────────────────────────────────────
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 2: No Arbitrage                 ");
        System.out.println("══════════════════════════════════════════");
        ArbitrageDetector d2 = new ArbitrageDetector();
        d2.addRate("USD", "EUR", 0.9);
        d2.addRate("EUR", "GBP", 0.8);
        d2.addRate("GBP", "USD", 1.2);   // 0.9×0.8×1.2 = 0.864 → loss
        d2.addRate("EUR", "USD", 1.10);
        d2.addRate("GBP", "EUR", 1.24);
        d2.addRate("USD", "GBP", 0.73);
        d2.printRates();
        System.out.println("Has arbitrage: " + d2.hasArbitrage());

        // ── 3. Multi-currency ─────────────────────────────────────────────────
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 3: 4-currency graph             ");
        System.out.println("══════════════════════════════════════════");
        ArbitrageDetector d3 = new ArbitrageDetector();
        d3.addRate("USD", "EUR", 0.85);
        d3.addRate("EUR", "JPY", 130.0);
        d3.addRate("JPY", "INR", 0.67);
        d3.addRate("INR", "USD", 0.013);  // USD→EUR→JPY→INR→USD = 0.85×130×0.67×0.013 ≈ 0.964 → no
        d3.addRate("JPY", "USD", 0.0093);
        d3.addRate("EUR", "USD", 1.18);
        d3.addRate("USD", "JPY", 110.0);
        // Add one rate that creates arbitrage: JPY→INR bump
        d3.addRate("INR", "EUR", 0.011);  // USD→EUR→JPY→INR→EUR→USD?
        d3.addRate("EUR", "INR", 92.0);   // EUR→INR→EUR cycle?
        d3.addRate("INR", "JPY", 1.6);    // creates EUR→INR→JPY→EUR? 92×1.6÷130≈1.13 → arbitrage!
        d3.printRates();
        System.out.println("Has arbitrage: " + d3.hasArbitrage());
        List<String> path3 = d3.getArbitragePath();
        System.out.println("Arbitrage path: " + path3);
        if (!path3.isEmpty()) {
            System.out.println("Profit trace:");
            double p = d3.cycleProfit(path3);
            System.out.printf("Net multiplier: %.6f  (%.2f%% gain)%n", p, (p - 1) * 100);
        }
    }
}

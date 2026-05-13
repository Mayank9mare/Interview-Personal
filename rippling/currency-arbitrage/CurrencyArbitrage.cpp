#include <bits/stdc++.h>
using namespace std;

// ═══════════════════════════════════════════════════════════════════════════
// Currency Arbitrage Detection
//
// Problem: Given exchange rates between currencies, detect whether a sequence
// of exchanges that starts and ends at the same currency yields a net profit.
//
// Example (arbitrage):
//   USD → EUR (×0.9)  →  EUR → GBP (×0.8)  →  GBP → USD (×1.5)
//   Net: 0.9 × 0.8 × 1.5 = 1.08  >  1.0  →  8% profit per cycle
//
// Key transformation:
//   Assign edge weight = -ln(rate).
//   Profitable cycle (product > 1) ↔ negative cycle (sum < 0) in log graph.
//
// Algorithm: Bellman-Ford
//   Init dist[all] = 0 (super-source: start with 1 unit of every currency).
//   Run V relaxations. Any node relaxed in the V-th round is on or reachable
//   from a negative cycle.
//   Extract cycle via predecessor pointers.
//
// addRate(from, to, rate)  — add directed exchange rate
// hasArbitrage()           — bool
// getArbitragePath()       — the profitable cycle e.g. ["USD","EUR","GBP","USD"]
//
// Complexity: O(V · E)
// ═══════════════════════════════════════════════════════════════════════════

struct ArbitrageDetector {
    static constexpr double EPS = 1e-10;

    map<string, int>       idx;
    vector<string>         currencies;
    vector<tuple<int,int,double>>  edges;         // {from_idx, to_idx, -ln(rate)}
    vector<pair<string,string>>    edgeCurrencies; // for display

    void addRate(const string& from, const string& to, double rate) {
        auto intern = [&](const string& c) {
            if (!idx.count(c)) { idx[c] = currencies.size(); currencies.push_back(c); }
        };
        intern(from); intern(to);
        edges.push_back({idx.at(from), idx.at(to), -log(rate)});
        edgeCurrencies.push_back({from, to});
    }

    bool hasArbitrage() {
        int V = currencies.size();
        vector<double> dist(V, 0.0);
        // V-1 relaxations
        for (int i = 0; i < V - 1; i++) relax(dist, nullptr);
        // V-th pass: any relaxation = negative cycle
        vector<double> before = dist;
        relax(dist, nullptr);
        for (int i = 0; i < V; i++) if (dist[i] < before[i] - EPS) return true;
        return false;
    }

    // Returns the arbitrage cycle or empty if none.
    vector<string> getArbitragePath() {
        int V = currencies.size();
        vector<double> dist(V, 0.0);
        vector<int>    pred(V, -1);

        int lastRelaxed = -1;
        for (int i = 0; i < V; i++)
            lastRelaxed = relax(dist, &pred);

        if (lastRelaxed == -1) return {};

        // Walk back V steps from lastRelaxed to land inside the cycle
        int inCycle = lastRelaxed;
        for (int i = 0; i < V; i++) inCycle = pred[inCycle];

        // Trace the cycle
        vector<string> cycle;
        unordered_set<int> seen;
        int curr = inCycle;
        while (seen.insert(curr).second) {
            cycle.push_back(currencies[curr]);
            curr = pred[curr];
        }
        cycle.push_back(currencies[curr]); // close
        reverse(cycle.begin(), cycle.end());
        return cycle;
    }

    double cycleProfit(const vector<string>& path) {
        map<pair<string,string>, double> rateMap;
        for (int i = 0; i < (int)edges.size(); i++) {
            string f = edgeCurrencies[i].first, t = edgeCurrencies[i].second;
            rateMap[{f, t}] = exp(-get<2>(edges[i]));
        }
        double product = 1.0;
        for (int i = 0; i + 1 < (int)path.size(); i++) {
            double r = rateMap[{path[i], path[i+1]}];
            product *= r;
            printf("  %s -> %s  x%.4f  (running: %.6f)\n",
                   path[i].c_str(), path[i+1].c_str(), r, product);
        }
        return product;
    }

    void printRates() {
        printf("Exchange rates:\n");
        for (int i = 0; i < (int)edges.size(); i++) {
            printf("  %-5s -> %-5s  %.4f\n",
                   edgeCurrencies[i].first.c_str(),
                   edgeCurrencies[i].second.c_str(),
                   exp(-get<2>(edges[i])));
        }
    }

private:
    // One Bellman-Ford relaxation pass; returns last node relaxed (-1 if none)
    int relax(vector<double>& dist, vector<int>* pred) {
        int lastRelaxed = -1;
        for (int i = 0; i < (int)edges.size(); i++) {
            int u = get<0>(edges[i]), v = get<1>(edges[i]);
            double w = get<2>(edges[i]);
            if (dist[u] + w < dist[v] - EPS) {
                dist[v] = dist[u] + w;
                if (pred) (*pred)[v] = u;
                lastRelaxed = v;
            }
        }
        return lastRelaxed;
    }
};

int main() {
    // ── 1. Arbitrage exists ───────────────────────────────────────────────
    printf("==========================================\n");
    printf(" Example 1: Arbitrage (3-currency cycle) \n");
    printf("==========================================\n");
    ArbitrageDetector d1;
    d1.addRate("USD", "EUR", 0.9);
    d1.addRate("EUR", "GBP", 0.8);
    d1.addRate("GBP", "USD", 1.5);   // 0.9×0.8×1.5 = 1.08 → profit
    d1.addRate("EUR", "USD", 1.11);
    d1.addRate("GBP", "EUR", 1.25);
    d1.addRate("USD", "GBP", 0.74);
    d1.printRates();
    printf("Has arbitrage: %s\n", d1.hasArbitrage() ? "true" : "false");
    auto path1 = d1.getArbitragePath();
    printf("Arbitrage path: [");
    for (int i = 0; i < (int)path1.size(); i++)
        printf("%s%s", path1[i].c_str(), i+1 < (int)path1.size() ? ", " : "");
    printf("]\n");
    if (!path1.empty()) {
        printf("Profit trace:\n");
        double profit = d1.cycleProfit(path1);
        printf("Net multiplier: %.6f  (%.2f%% gain)\n", profit, (profit - 1) * 100);
    }

    // ── 2. No arbitrage ───────────────────────────────────────────────────
    printf("\n==========================================\n");
    printf(" Example 2: No Arbitrage                 \n");
    printf("==========================================\n");
    ArbitrageDetector d2;
    d2.addRate("USD", "EUR", 0.9);
    d2.addRate("EUR", "GBP", 0.8);
    d2.addRate("GBP", "USD", 1.2);   // 0.9×0.8×1.2 = 0.864 → loss
    d2.addRate("EUR", "USD", 1.10);
    d2.addRate("GBP", "EUR", 1.24);
    d2.addRate("USD", "GBP", 0.73);
    d2.printRates();
    printf("Has arbitrage: %s\n", d2.hasArbitrage() ? "true" : "false");

    // ── 3. 4-currency graph ───────────────────────────────────────────────
    printf("\n==========================================\n");
    printf(" Example 3: 4-currency graph             \n");
    printf("==========================================\n");
    ArbitrageDetector d3;
    d3.addRate("USD", "EUR", 0.85);
    d3.addRate("EUR", "JPY", 130.0);
    d3.addRate("JPY", "INR", 0.67);
    d3.addRate("INR", "USD", 0.013);
    d3.addRate("JPY", "USD", 0.0093);
    d3.addRate("EUR", "USD", 1.18);
    d3.addRate("USD", "JPY", 110.0);
    d3.addRate("INR", "EUR", 0.011);
    d3.addRate("EUR", "INR", 92.0);
    d3.addRate("INR", "JPY", 1.6);   // EUR→INR→JPY→? creates arbitrage
    d3.printRates();
    printf("Has arbitrage: %s\n", d3.hasArbitrage() ? "true" : "false");
    auto path3 = d3.getArbitragePath();
    printf("Arbitrage path: [");
    for (int i = 0; i < (int)path3.size(); i++)
        printf("%s%s", path3[i].c_str(), i+1 < (int)path3.size() ? ", " : "");
    printf("]\n");
    if (!path3.empty()) {
        printf("Profit trace:\n");
        double p = d3.cycleProfit(path3);
        printf("Net multiplier: %.6f  (%.2f%% gain)\n", p, (p - 1) * 100);
    }

    return 0;
}

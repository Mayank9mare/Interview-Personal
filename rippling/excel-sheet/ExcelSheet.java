import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

public class ExcelSheet {

    // ═══════════════════════════════════════════════════════════════════════════
    // Excel Sheet
    //
    //   set(cell, value)   — cell is "A1", "B3", etc.
    //                        value is a number ("42", "3.14") or a formula ("=A1+B2*3")
    //   print()            — renders a grid showing raw and computed value per cell
    //
    // Formula support:
    //   +  -  *  /  ( )  cell references  unary minus
    //   e.g. "=A1+B1", "=(A1+B1)*C2", "=-3", "=A1*-1"
    //
    // Evaluation:
    //   Recursive descent parser with operator precedence.
    //   Cell references resolved lazily; results cached per print() call.
    //   Circular dependency detected via a DFS "visiting" set → #CIRC error.
    //   Unset cells evaluate to 0.
    //
    // Data structures:
    //   raw  : LinkedHashMap<cell, raw-string>   — preserves insertion order for printing
    //   deps : Map<cell, Set<cell>>              — direct references (for cycle detection)
    //
    // Complexity per print():
    //   Each cell evaluated once (memoised cache).  O(n) total where n = cells set.
    //   Parser runs in O(|expr|) per cell.
    // ═══════════════════════════════════════════════════════════════════════════

    // ── Recursive-descent parser ──────────────────────────────────────────────
    // Grammar:
    //   expr   = term   (('+' | '-') term)*
    //   term   = factor (('*' | '/') factor)*
    //   factor = '(' expr ')' | '-' factor | NUMBER | CELL_REF
    //
    // CELL_REF resolution is delegated to a Function<String,Double> so the
    // parser stays decoupled from the Sheet's cycle-detection machinery.
    // ─────────────────────────────────────────────────────────────────────────
    static class Parser {
        private final String s;
        private int pos;
        private final Function<String, Double> resolver;

        Parser(String expr, Function<String, Double> resolver) {
            this.s        = expr.replaceAll("\\s+", ""); // strip all whitespace
            this.resolver = resolver;
        }

        double parse() { double v = parseExpr(); return v; }

        private double parseExpr() {
            double val = parseTerm();
            while (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
                char op = s.charAt(pos++);
                double r = parseTerm();
                val = (op == '+') ? val + r : val - r;
            }
            return val;
        }

        private double parseTerm() {
            double val = parseFactor();
            while (pos < s.length() && (s.charAt(pos) == '*' || s.charAt(pos) == '/')) {
                char op = s.charAt(pos++);
                double r = parseFactor();
                if (op == '/' && r == 0) throw new ArithmeticException("div/0");
                val = (op == '*') ? val * r : val / r;
            }
            return val;
        }

        private double parseFactor() {
            if (pos >= s.length()) throw new IllegalArgumentException("Unexpected end");
            char c = s.charAt(pos);

            if (c == '(') {
                pos++;
                double val = parseExpr();
                if (pos >= s.length() || s.charAt(pos) != ')')
                    throw new IllegalArgumentException("Missing ')'");
                pos++;
                return val;
            }

            // Unary minus: only when NOT preceded by a digit/cell (i.e. truly unary)
            if (c == '-') { pos++; return -parseFactor(); }

            // Cell reference: one or more letters followed by one or more digits
            if (Character.isLetter(c)) {
                int start = pos;
                while (pos < s.length() && Character.isLetter(s.charAt(pos))) pos++;
                while (pos < s.length() && Character.isDigit(s.charAt(pos)))  pos++;
                return resolver.apply(s.substring(start, pos).toUpperCase());
            }

            // Number literal
            if (Character.isDigit(c) || c == '.') {
                int start = pos;
                while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
                return Double.parseDouble(s.substring(start, pos));
            }

            throw new IllegalArgumentException("Unexpected char '" + c + "' at pos " + pos);
        }
    }

    // ── Sheet ─────────────────────────────────────────────────────────────────
    static class Sheet {
        private final Map<String, String> raw  = new LinkedHashMap<>();

        public void set(String cell, String value) {
            raw.put(cell.toUpperCase().trim(), value.trim());
        }

        // Evaluate one cell (public entry point)
        public double evaluate(String cell) {
            return eval(cell.toUpperCase(), new HashSet<>(), new HashMap<>());
        }

        private double eval(String cell, Set<String> visiting, Map<String, Double> cache) {
            if (cache.containsKey(cell)) return cache.get(cell);
            if (visiting.contains(cell))
                throw new IllegalStateException("CIRC:" + cell);

            String expr = raw.get(cell);
            if (expr == null) return 0.0;   // unset cell = 0

            double val;
            if (!expr.startsWith("=")) {
                val = Double.parseDouble(expr);
            } else {
                visiting.add(cell);
                Function<String, Double> resolver = ref -> eval(ref, visiting, cache);
                val = new Parser(expr.substring(1), resolver).parse();
                visiting.remove(cell);
            }
            cache.put(cell, val);
            return val;
        }

        // ── Print ─────────────────────────────────────────────────────────────

        public void print() {
            if (raw.isEmpty()) { System.out.println("(empty)"); return; }

            // Find grid bounds
            int maxRow = 0, maxCol = 0;
            for (String cell : raw.keySet()) {
                int[] rc = cellToRowCol(cell);
                maxRow = Math.max(maxRow, rc[0]);
                maxCol = Math.max(maxCol, rc[1]);
            }

            // Pre-compute all values (shared cache so each cell evaluated once)
            Map<String, Double>  cache   = new HashMap<>();
            Map<String, String>  display = new LinkedHashMap<>();
            for (String cell : raw.keySet()) {
                try {
                    double v = eval(cell, new HashSet<>(), cache);
                    display.put(cell, fmt(v));
                } catch (Exception e) {
                    display.put(cell, "#" + e.getMessage().split(":")[0]);
                }
            }

            int RAW  = 16;   // column width for raw
            int COMP = 10;   // column width for computed

            // ── Header row ────────────────────────────────────────────────────
            String colHdr = "     ";
            for (int c = 0; c <= maxCol; c++) {
                String label = colLabel(c);
                // centre label over [RAW + 3 + COMP] wide block
                int blockW = RAW + 3 + COMP;
                colHdr += centred(label, blockW) + " | ";
            }
            String divider = "-".repeat(colHdr.length());
            System.out.println(divider);
            System.out.println(colHdr);
            System.out.println(divider);

            // ── Data rows ─────────────────────────────────────────────────────
            for (int r = 1; r <= maxRow; r++) {
                StringBuilder rawLine  = new StringBuilder(String.format("%4d ", r));
                StringBuilder compLine = new StringBuilder("     ");

                for (int c = 0; c <= maxCol; c++) {
                    String cell  = colLabel(c) + r;
                    String rawV  = raw.getOrDefault(cell, "");
                    String compV = display.getOrDefault(cell, "");

                    rawLine .append(String.format("%-" + RAW + "s", rawV.isEmpty()  ? "-" : rawV));
                    rawLine .append(" → ");
                    rawLine .append(String.format("%-" + COMP + "s", compV.isEmpty() ? "-" : compV));
                    rawLine .append(" | ");

                    compLine.append(" ".repeat(RAW + 3 + COMP));
                    compLine.append(" | ");
                }
                System.out.println(rawLine);
            }
            System.out.println(divider);
        }

        // ── Utility ───────────────────────────────────────────────────────────

        // "A1" → [row=1, col=0],  "B3" → [row=3, col=1],  "AA2" → [row=2, col=26]
        private int[] cellToRowCol(String cell) {
            int i = 0;
            while (i < cell.length() && Character.isLetter(cell.charAt(i))) i++;
            String colStr = cell.substring(0, i);
            int row = Integer.parseInt(cell.substring(i));
            int col = 0;
            for (char ch : colStr.toCharArray()) col = col * 26 + (ch - 'A' + 1);
            return new int[]{row, col - 1};
        }

        // 0→"A", 1→"B", …, 25→"Z", 26→"AA", …
        private String colLabel(int idx) {
            StringBuilder sb = new StringBuilder();
            for (int n = idx + 1; n > 0; n = (n - 1) / 26)
                sb.insert(0, (char)('A' + (n - 1) % 26));
            return sb.toString();
        }

        private String fmt(double v) {
            return (v == Math.floor(v) && !Double.isInfinite(v))
                   ? String.valueOf((long) v)
                   : String.format("%.4f", v);
        }

        private String centred(String s, int width) {
            int pad = Math.max(0, width - s.length());
            int l = pad / 2, r = pad - l;
            return " ".repeat(l) + s + " ".repeat(r);
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════");
        System.out.println(" Basic arithmetic & cross-references ");
        System.out.println("══════════════════════════════════════");
        Sheet s = new Sheet();
        s.set("A1", "5");
        s.set("B1", "3");
        s.set("C1", "=A1+B1");          // 8
        s.set("A2", "=A1*2");           // 10
        s.set("B2", "=A2+B1");          // 13
        s.set("C2", "=C1*B2");          // 8 * 13 = 104
        s.print();

        System.out.println("\n══════════════════════════════════════");
        System.out.println(" After updating A1 = 10              ");
        System.out.println("══════════════════════════════════════");
        s.set("A1", "10");
        s.print();
        // C1 = 13,  A2 = 20,  B2 = 23,  C2 = 13*23 = 299

        System.out.println("\n══════════════════════════════════════");
        System.out.println(" Parentheses, unary minus, division  ");
        System.out.println("══════════════════════════════════════");
        Sheet s2 = new Sheet();
        s2.set("A1", "6");
        s2.set("B1", "2");
        s2.set("C1", "=(A1+B1)*3");     // (6+2)*3 = 24
        s2.set("A2", "=A1/B1");         // 3
        s2.set("B2", "=-A1");           // -6
        s2.set("C2", "=C1+B2");         // 24 + (-6) = 18
        s2.print();

        System.out.println("\n══════════════════════════════════════");
        System.out.println(" Circular dependency → #CIRC        ");
        System.out.println("══════════════════════════════════════");
        Sheet s3 = new Sheet();
        s3.set("X1", "=Y1+1");
        s3.set("Y1", "=X1+1");
        s3.set("Z1", "10");
        s3.print();   // X1 and Y1 show #CIRC, Z1 shows 10

        System.out.println("\n══════════════════════════════════════");
        System.out.println(" Diamond dependency (shared ref)     ");
        System.out.println("══════════════════════════════════════");
        // A1 is referenced by both B1 and C1, then D1 uses both.
        // A1 must be evaluated only once (cache).
        Sheet s4 = new Sheet();
        s4.set("A1", "7");
        s4.set("B1", "=A1*2");          // 14
        s4.set("C1", "=A1+1");          // 8
        s4.set("D1", "=B1+C1");         // 14+8 = 22  (A1 evaluated once)
        s4.print();
    }
}

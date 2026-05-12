-- ─────────────────────────────────────────────────────────────────────────
-- SQL Interview Reference
-- ─────────────────────────────────────────────────────────────────────────
-- Schema used throughout:
--   employees(id, name, dept_id, salary, manager_id, hire_date)
--   departments(id, name)
--   orders(id, customer_id, amount, order_date)
--   customers(id, name, city)
--   products(id, name, price, category)
--   order_items(order_id, product_id, quantity)
-- ─────────────────────────────────────────────────────────────────────────


-- ─────────────────────────────────────────────────────────────────────────
-- 1. BASICS
-- ─────────────────────────────────────────────────────────────────────────

SELECT name, salary
FROM   employees
WHERE  salary > 50000
  AND  dept_id = 3
ORDER  BY salary DESC
LIMIT  10 OFFSET 20;      -- page 3 at 10 per page

-- DISTINCT — deduplicate result rows
SELECT DISTINCT dept_id FROM employees;

-- BETWEEN / IN / LIKE / IS NULL
SELECT * FROM employees
WHERE  salary    BETWEEN 50000 AND 100000
  AND  dept_id   IN (1, 2, 3)
  AND  name      LIKE 'A%'            -- starts with A; '%A%' = contains A; 'A_' = A then any char
  AND  manager_id IS NOT NULL;

-- CASE WHEN — inline conditional
SELECT name, salary,
       CASE
           WHEN salary >= 100000 THEN 'Senior'
           WHEN salary >= 70000  THEN 'Mid'
           ELSE                       'Junior'
       END AS level
FROM employees;

-- COALESCE — first non-NULL value
SELECT name, COALESCE(manager_id, -1) AS mgr_id FROM employees;

-- NULLIF(a, b) — returns NULL if a = b; prevents divide-by-zero
SELECT name, total_sales / NULLIF(num_orders, 0) AS avg_order FROM sales_summary;


-- ─────────────────────────────────────────────────────────────────────────
-- 2. AGGREGATE FUNCTIONS + GROUP BY + HAVING
-- ─────────────────────────────────────────────────────────────────────────

-- COUNT(*) counts all rows; COUNT(col) ignores NULLs
SELECT
    dept_id,
    COUNT(*)          AS headcount,
    AVG(salary)       AS avg_salary,
    MAX(salary)       AS max_salary,
    MIN(salary)       AS min_salary,
    SUM(salary)       AS total_payroll
FROM   employees
GROUP  BY dept_id
HAVING COUNT(*) >= 5          -- HAVING filters on aggregates (WHERE runs before GROUP BY)
ORDER  BY avg_salary DESC;

-- WHERE vs HAVING:
--   WHERE  filters individual rows BEFORE grouping
--   HAVING filters groups AFTER aggregation


-- ─────────────────────────────────────────────────────────────────────────
-- 3. JOINS
-- ─────────────────────────────────────────────────────────────────────────

-- INNER JOIN — only rows with a match in both tables
SELECT e.name, d.name AS dept
FROM   employees e
JOIN   departments d ON e.dept_id = d.id;

-- LEFT JOIN — all from left; NULL columns if no match on right
SELECT e.name, d.name AS dept
FROM   employees   e
LEFT   JOIN departments d ON e.dept_id = d.id;   -- employees with no dept still appear

-- Find employees with MISSING dept (anti-join)
SELECT e.*
FROM   employees   e
LEFT   JOIN departments d ON e.dept_id = d.id
WHERE  d.id IS NULL;

-- RIGHT JOIN — all from right (prefer left-join with tables swapped)
SELECT e.name, d.name
FROM   departments d
LEFT   JOIN employees e ON e.dept_id = d.id;    -- all depts, including empty ones

-- FULL OUTER JOIN — all rows from both; NULL where no match
SELECT e.name, d.name
FROM   employees   e
FULL   OUTER JOIN departments d ON e.dept_id = d.id;

-- SELF JOIN — join table to itself (hierarchies, comparisons within same table)
SELECT e.name AS employee, m.name AS manager
FROM   employees e
LEFT   JOIN employees m ON e.manager_id = m.id;

-- CROSS JOIN — cartesian product (use carefully — O(m*n) rows)
SELECT e.name, d.name
FROM   employees e CROSS JOIN departments d;

-- Multi-table join
SELECT e.name, d.name AS dept, o.amount
FROM   employees   e
JOIN   departments d ON e.dept_id     = d.id
JOIN   orders      o ON o.customer_id = e.id;


-- ─────────────────────────────────────────────────────────────────────────
-- 4. SUBQUERIES
-- ─────────────────────────────────────────────────────────────────────────

-- Scalar subquery — returns single value
SELECT name, salary
FROM   employees
WHERE  salary = (SELECT MAX(salary) FROM employees);

-- IN subquery — returns a set
SELECT name
FROM   employees
WHERE  dept_id IN (SELECT id FROM departments WHERE name = 'Engineering');

-- EXISTS — stops at first match (faster than IN on large sets)
SELECT d.name
FROM   departments d
WHERE  EXISTS (SELECT 1 FROM employees e WHERE e.dept_id = d.id);

-- NOT EXISTS — depts with no employees
SELECT d.name
FROM   departments d
WHERE  NOT EXISTS (SELECT 1 FROM employees e WHERE e.dept_id = d.id);

-- Correlated subquery — references outer query; runs once per outer row
-- Employees earning above their department average
SELECT e.name, e.salary, e.dept_id
FROM   employees e
WHERE  e.salary > (
           SELECT AVG(salary) FROM employees WHERE dept_id = e.dept_id
       );

-- Derived table (subquery in FROM clause)
SELECT dept_id, avg_sal
FROM  (SELECT dept_id, AVG(salary) AS avg_sal
       FROM   employees
       GROUP  BY dept_id) t
WHERE  avg_sal > 80000;


-- ─────────────────────────────────────────────────────────────────────────
-- 5. CTEs (WITH clause)
--    Named subquery — improves readability, can reference multiple times
--    Materialised differently per DB (PostgreSQL materialises by default)
-- ─────────────────────────────────────────────────────────────────────────

-- Simple CTE
WITH dept_avg AS (
    SELECT dept_id, AVG(salary) AS avg_sal
    FROM   employees
    GROUP  BY dept_id
)
SELECT e.name, e.salary, da.avg_sal
FROM   employees e
JOIN   dept_avg  da ON e.dept_id = da.dept_id
WHERE  e.salary > da.avg_sal;

-- Multiple CTEs — chain them
WITH
high_earners AS (
    SELECT * FROM employees WHERE salary > 100000
),
eng_depts AS (
    SELECT id FROM departments WHERE name LIKE '%Eng%'
)
SELECT h.name
FROM   high_earners h
WHERE  h.dept_id IN (SELECT id FROM eng_depts);

-- Recursive CTE — traverse tree / hierarchy
WITH RECURSIVE org_tree AS (
    -- Anchor: root nodes (no manager)
    SELECT id, name, manager_id, 0 AS depth
    FROM   employees
    WHERE  manager_id IS NULL

    UNION ALL

    -- Recursive: find each node's direct reports
    SELECT e.id, e.name, e.manager_id, ot.depth + 1
    FROM   employees e
    JOIN   org_tree  ot ON e.manager_id = ot.id
)
SELECT name, depth FROM org_tree ORDER BY depth, name;


-- ─────────────────────────────────────────────────────────────────────────
-- 6. WINDOW FUNCTIONS
--    Compute over a "window" of rows without collapsing them (unlike GROUP BY)
--    Syntax: function() OVER (PARTITION BY ... ORDER BY ... ROWS/RANGE ...)
-- ─────────────────────────────────────────────────────────────────────────

-- Ranking functions
--   ROW_NUMBER  — unique sequential (no ties):           1, 2, 3, 4
--   RANK        — ties share rank, then gaps:            1, 1, 3, 4
--   DENSE_RANK  — ties share rank, no gaps:             1, 1, 2, 3
--   NTILE(n)    — split into n buckets, return bucket # (1..n)
SELECT
    name, dept_id, salary,
    ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS row_num,
    RANK()       OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rank,
    DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS dense_rank,
    NTILE(4)     OVER (PARTITION BY dept_id ORDER BY salary DESC) AS quartile
FROM employees;

-- Aggregate window functions — running / cumulative
SELECT
    name, dept_id, salary, hire_date,
    SUM(salary) OVER (PARTITION BY dept_id ORDER BY hire_date)   AS running_payroll,
    AVG(salary) OVER (PARTITION BY dept_id)                      AS dept_avg_sal,
    salary - AVG(salary) OVER (PARTITION BY dept_id)             AS diff_from_avg,
    COUNT(*)    OVER (PARTITION BY dept_id)                      AS dept_headcount,
    MAX(salary) OVER ()                                          AS company_max_sal
FROM employees;

-- LAG / LEAD — access previous / next row's value
SELECT
    name, hire_date, salary,
    LAG(salary)  OVER (PARTITION BY dept_id ORDER BY hire_date)  AS prev_hire_sal,
    LEAD(salary) OVER (PARTITION BY dept_id ORDER BY hire_date)  AS next_hire_sal,
    salary - LAG(salary) OVER (PARTITION BY dept_id ORDER BY hire_date) AS sal_change
FROM employees;

-- FIRST_VALUE / LAST_VALUE
SELECT
    name, dept_id, salary,
    FIRST_VALUE(name) OVER (PARTITION BY dept_id ORDER BY salary DESC) AS highest_earner,
    -- LAST_VALUE needs explicit frame; default frame stops at current row
    LAST_VALUE(name) OVER (
        PARTITION BY dept_id ORDER BY salary DESC
        ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
    ) AS lowest_earner
FROM employees;

-- Moving average (last 3 rows including current)
SELECT
    order_date, amount,
    AVG(amount) OVER (
        ORDER BY order_date
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
    ) AS moving_avg_3
FROM orders;


-- ─────────────────────────────────────────────────────────────────────────
-- 7. COMMON INTERVIEW PATTERNS
-- ─────────────────────────────────────────────────────────────────────────

-- Nth highest salary (2nd highest)
-- Option A: OFFSET
SELECT DISTINCT salary FROM employees ORDER BY salary DESC LIMIT 1 OFFSET 1;

-- Option B: window function (handles ties cleanly)
SELECT name, salary
FROM  (SELECT name, salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk FROM employees) t
WHERE  rnk = 2;

-- Top N per group — top 3 earners per department
SELECT name, dept_id, salary
FROM  (SELECT name, dept_id, salary,
              ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rn
       FROM   employees) t
WHERE  rn <= 3;

-- Duplicate rows — find names that appear more than once
SELECT name, COUNT(*) AS cnt
FROM   employees
GROUP  BY name
HAVING COUNT(*) > 1;

-- Delete duplicates — keep only the row with lowest id
DELETE FROM employees
WHERE  id NOT IN (SELECT MIN(id) FROM employees GROUP BY name, dept_id, salary);

-- Running total
SELECT order_date, amount,
       SUM(amount) OVER (ORDER BY order_date) AS running_total
FROM   orders;

-- Month-over-month revenue change
WITH monthly AS (
    SELECT DATE_TRUNC('month', order_date) AS month,
           SUM(amount) AS revenue
    FROM   orders
    GROUP  BY 1
)
SELECT month, revenue,
       LAG(revenue) OVER (ORDER BY month)                          AS prev_month,
       revenue - LAG(revenue) OVER (ORDER BY month)                AS change,
       ROUND(100.0 * (revenue - LAG(revenue) OVER (ORDER BY month))
             / NULLIF(LAG(revenue) OVER (ORDER BY month), 0), 2)   AS pct_change
FROM   monthly;

-- Consecutive login streak (gap-and-island technique)
-- Idea: subtract row_number from date; consecutive dates produce the same group value
WITH numbered AS (
    SELECT user_id, login_date,
           login_date - (ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date)
                         * INTERVAL '1 day') AS grp
    FROM   logins
)
SELECT user_id, MIN(login_date) AS streak_start, MAX(login_date) AS streak_end,
       COUNT(*) AS streak_len
FROM   numbered
GROUP  BY user_id, grp
ORDER  BY streak_len DESC;

-- Pivot — rows to columns
SELECT
    SUM(CASE WHEN d.name = 'Engineering' THEN e.salary ELSE 0 END) AS eng_payroll,
    SUM(CASE WHEN d.name = 'HR'          THEN e.salary ELSE 0 END) AS hr_payroll,
    SUM(CASE WHEN d.name = 'Sales'       THEN e.salary ELSE 0 END) AS sales_payroll
FROM   employees e
JOIN   departments d ON e.dept_id = d.id;

-- Median salary
SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) AS median_salary
FROM   employees;

-- Customers who bought product A but NOT product B
SELECT DISTINCT o.customer_id
FROM   orders      o
JOIN   order_items oi ON o.id = oi.order_id
WHERE  oi.product_id = 1                   -- bought A
  AND  o.customer_id NOT IN (
           SELECT DISTINCT o2.customer_id
           FROM   orders      o2
           JOIN   order_items oi2 ON o2.id = oi2.order_id
           WHERE  oi2.product_id = 2       -- bought B
       );

-- Second most recent order per customer
SELECT customer_id, order_date, amount
FROM  (SELECT customer_id, order_date, amount,
              ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY order_date DESC) AS rn
       FROM   orders) t
WHERE  rn = 2;

-- Employees with salary above company average AND above their dept average
WITH averages AS (
    SELECT dept_id,
           AVG(salary)                  AS dept_avg,
           AVG(salary) OVER ()          AS company_avg  -- window over full table
    FROM   employees
    GROUP  BY dept_id
)
SELECT e.name, e.salary, a.dept_avg, a.company_avg
FROM   employees e
JOIN   averages  a ON e.dept_id = a.dept_id
WHERE  e.salary > a.dept_avg
  AND  e.salary > a.company_avg;

-- Hierarchical query: all subordinates of a given manager (recursive CTE)
WITH RECURSIVE subordinates AS (
    SELECT id, name, manager_id FROM employees WHERE id = 5  -- root manager
    UNION ALL
    SELECT e.id, e.name, e.manager_id
    FROM   employees e
    JOIN   subordinates s ON e.manager_id = s.id
)
SELECT * FROM subordinates;


-- ─────────────────────────────────────────────────────────────────────────
-- 8. INDEXES & PERFORMANCE
-- ─────────────────────────────────────────────────────────────────────────

CREATE INDEX idx_emp_dept     ON employees(dept_id);
CREATE INDEX idx_emp_sal      ON employees(salary);
CREATE INDEX idx_emp_dept_sal ON employees(dept_id, salary);  -- composite
CREATE UNIQUE INDEX idx_emp_email ON employees(email);

-- Index DO use for: WHERE, JOIN ON, ORDER BY, GROUP BY columns
-- Index DON'T:
--   Function on indexed col:  WHERE LOWER(email) = 'a@b.com'  ← ignores index on email
--   Leading wildcard:          WHERE name LIKE '%smith'         ← can't use B-tree
--   Too many indexes            ← slows INSERT/UPDATE/DELETE

-- Composite index column order matters:
--   (dept_id, salary) supports: WHERE dept_id=? and WHERE dept_id=? AND salary>?
--   Does NOT efficiently support: WHERE salary>? alone (missing leading column)

-- Covering index — include all queried columns to avoid table lookup
CREATE INDEX idx_covering ON employees(dept_id, salary) INCLUDE (name);

-- EXPLAIN / EXPLAIN ANALYZE — inspect query plan
EXPLAIN        SELECT * FROM employees WHERE dept_id = 3;
EXPLAIN ANALYZE SELECT * FROM employees WHERE salary > 80000;
-- Look for: Seq Scan (full table) vs Index Scan vs Index Only Scan


-- ─────────────────────────────────────────────────────────────────────────
-- 9. SET OPERATIONS
-- ─────────────────────────────────────────────────────────────────────────

-- UNION      — combine, remove duplicates  (adds sort overhead)
-- UNION ALL  — combine, keep duplicates    (faster — no dedup)
-- INTERSECT  — rows present in BOTH
-- EXCEPT     — rows in first but NOT second  (MySQL: EXCEPT not supported → use NOT IN)

SELECT id FROM current_employees
UNION ALL
SELECT id FROM archived_employees;

SELECT dept_id FROM employees WHERE salary > 80000
INTERSECT
SELECT id FROM departments WHERE name LIKE 'Eng%';

SELECT id FROM all_users
EXCEPT
SELECT user_id FROM banned_users;


-- ─────────────────────────────────────────────────────────────────────────
-- 10. TRANSACTIONS & ISOLATION LEVELS
-- ─────────────────────────────────────────────────────────────────────────

BEGIN;
    UPDATE accounts SET balance = balance - 100 WHERE id = 1;
    UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;   -- or ROLLBACK to undo

-- Isolation levels (low → high):
--   READ UNCOMMITTED — can read dirty (uncommitted) data
--   READ COMMITTED   — only read committed data (default in many DBs)
--   REPEATABLE READ  — same query returns same data within transaction (default in MySQL)
--   SERIALIZABLE     — full isolation; transactions appear sequential

-- Anomalies each level prevents:
--   Dirty read       — prevented at READ COMMITTED+
--   Non-repeatable  — prevented at REPEATABLE READ+
--   Phantom read     — prevented at SERIALIZABLE


-- ─────────────────────────────────────────────────────────────────────────
-- 11. QUICK REFERENCE — EXECUTION ORDER
--    FROM → JOIN → WHERE → GROUP BY → HAVING → SELECT → DISTINCT → ORDER BY → LIMIT
--
--    This order explains why:
--    - WHERE can't use aliases defined in SELECT
--    - HAVING can use aggregates; WHERE cannot
--    - ORDER BY CAN use SELECT aliases (runs after SELECT)
-- ─────────────────────────────────────────────────────────────────────────

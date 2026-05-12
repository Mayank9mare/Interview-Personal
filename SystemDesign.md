# System Design Reference

---

## PART 1 — Interview Framework (45-minute structure)

### The 5-Step Framework

```
Step 1  Clarify Requirements         (5 min)
Step 2  Estimate Scale               (5 min)
Step 3  High-Level Design            (10 min)
Step 4  Deep Dive into Components    (15 min)
Step 5  Trade-offs & Bottlenecks     (10 min)
```

### Step 1 — Clarify Requirements
**Always ask before drawing anything.**

- Functional: "What exactly should the system do?" — list the 3-5 core features
- Non-functional: "How big? How fast? How reliable?"
  - Scale: DAU, QPS, storage growth per year
  - Latency: p99 response time, real-time vs batch
  - Consistency: is stale data ok? eventual vs strong
  - Availability: 99.9% (8.7 hrs/year downtime) vs 99.99% (52 min/year)
- Constraints: "Is this mobile-first? Any regulatory requirements?"
- Out of scope: explicitly name what you're NOT building

### Step 2 — Estimate Scale
```
Users:        100M DAU
Reads:        10 requests/user/day → 100M * 10 / 86400 ≈ 12,000 QPS
Writes:       1 write / 10 reads   → 1,200 QPS
Storage:      1 post = 1 KB        → 1,200 * 86400 * 365 * 1KB ≈ 38 TB/year
Bandwidth:    12,000 QPS * 1 KB    ≈ 12 MB/s reads
```

### Step 3 — High-Level Design
- Draw: client → load balancer → API servers → cache → DB
- Identify: read-heavy vs write-heavy, synchronous vs async flows
- Choose: SQL vs NoSQL, monolith vs microservices, push vs pull

### Step 4 — Deep Dive
Interviewer picks 1-2 components. Go deep:
- Data model (tables, keys, indexes)
- Algorithm (matching, ranking, hashing)
- Bottleneck solution (sharding, caching, CDN)
- Failure mode (what if this component dies?)

### Step 5 — Trade-offs
- Always present options: "We could do X or Y; X gives us A but costs B"
- Address: single points of failure, hot spots, consistency windows
- Scale further: "To go 10x we'd need to shard the DB / add read replicas"

---

## PART 2 — Numbers Every Engineer Must Know

### Latency
```
L1 cache access          0.5 ns
L2 cache access          7   ns
RAM access               100 ns
SSD random read          150 μs
HDD seek                 10  ms
Network same datacenter  500 μs
Network cross-region     150 ms
```
**Rule of thumb:** RAM is 1000x faster than SSD; SSD is 1000x faster than disk.

### Throughput
```
Single server (commodity)     10,000–50,000 QPS
Redis                         100,000+  ops/sec
MySQL (with indexes)          5,000–20,000 QPS
Kafka                         1,000,000  msg/sec (per partition)
```

### Storage
```
1 KB    = short tweet / user record
1 MB    = 1 photo (compressed)
1 GB    = 1000 photos
1 TB    = 1M photos
1 PB    = 1B photos

1M users × 1 KB/user    = 1 GB
1B users × 1 KB/user    = 1 TB
100M messages/day × 100 bytes = 10 GB/day = 3.6 TB/year
```

### Bandwidth
```
Bandwidth = QPS × average_payload_size
100,000 QPS × 1 KB = 100 MB/s = ~1 Gbps
```

---

## PART 3 — Core Building Blocks

### 1. Load Balancer
| Type | Layer | Use case |
|------|-------|----------|
| L4 (Network LB) | TCP/UDP | Simple throughput, no HTTP inspection |
| L7 (Application LB) | HTTP/HTTPS | Routing by path/header, SSL termination |

**Algorithms:** Round robin → Weighted round robin → Least connections → Consistent hash (sticky sessions)

**Health checks:** LB pings `/health` every 5 s; removes unhealthy nodes automatically.

---

### 2. CDN
- **Pull CDN:** Origin serves first request; CDN caches it. Simple, lazy.
- **Push CDN:** You push content to CDN proactively. Good for large static assets (videos).
- **Cache-Control headers:** `max-age`, `s-maxage`, `Cache-Control: no-cache`
- Use CDN for: static assets, images, video, geographic latency reduction.

---

### 3. Database Selection Decision Tree
```
Need ACID transactions?
  └─ Yes → SQL (PostgreSQL, MySQL)
  └─ No → What's your access pattern?
       ├─ Document (flexible schema, nested objects) → MongoDB
       ├─ Key-value (simple lookup, high throughput) → Redis, DynamoDB
       ├─ Wide column (time-series, write-heavy, analytics) → Cassandra, HBase
       ├─ Graph (social networks, recommendations) → Neo4j
       └─ Search (full-text, fuzzy) → Elasticsearch
```

**Scaling SQL:**
1. Read replicas (leader-follower) → scale reads
2. Vertical scaling → bigger machine
3. Sharding (horizontal partitioning) → scale writes
   - Range-based: shard by user_id range → hot spots possible
   - Hash-based: shard_id = hash(user_id) % N → even but range queries hard
   - Directory-based: lookup table → flexible but lookup overhead

**Sharding pitfalls:** cross-shard joins, cross-shard transactions, resharding.

---

### 4. Caching (Redis)
**When to cache:** read-heavy (>10:1 read/write), expensive computations, session data.

**Eviction policies:**
- `LRU` (default): evict least recently used → good for temporal locality
- `LFU`: evict least frequently used → good for hot-item caches
- `TTL`: expire after fixed time → good for rate limiting, sessions

**Cache patterns:**

| Pattern | Read flow | Write flow | Consistency |
|---------|-----------|------------|-------------|
| Cache-aside (lazy) | Check cache → miss → read DB → populate cache | Write to DB only; invalidate cache | Eventual |
| Write-through | Read from cache | Write to DB AND cache atomically | Strong |
| Write-back | Read from cache | Write to cache; async flush to DB | Risk of loss |
| Read-through | Cache fetches from DB on miss | Write to DB only | Eventual |

**Cache invalidation strategies:**
- TTL expiry (simple, some staleness)
- Event-driven invalidation (Kafka event → delete cache key)
- Write-through (always consistent, extra latency on writes)

**Hot key problem:** one cache key gets 1M req/s → local in-process cache + jittered TTL.

---

### 5. Message Queue (Kafka)
```
Producer → Topic → [Partition 0] → Consumer Group A
                 → [Partition 1] → Consumer Group B
                 → [Partition 2]
```
- **Topic:** logical stream of events
- **Partition:** unit of parallelism; one consumer per partition per group
- **Offset:** position in partition; consumers commit offset after processing
- **Retention:** messages kept for N days regardless of consumption

**At-least-once vs Exactly-once:**
- At-least-once: re-deliver on failure → idempotent consumers required
- Exactly-once: two-phase commit → higher latency, Kafka Streams or transactional API

**When to use Kafka:** decouple producers/consumers, fan-out to multiple consumers, event replay, audit log, high-throughput pipelines.

**When to use RabbitMQ (AMQP):** complex routing, per-message TTL, priority queues, simpler setup.

---

### 6. API Gateway
Single entry point for all clients:
- **Authentication** (JWT validation, API key check)
- **Rate limiting** (per user, per endpoint)
- **SSL termination**
- **Request routing** (to microservices)
- **Request/response transformation**
- **Circuit breaking** (stop cascading failures)

---

### 7. Service Discovery
- **Client-side:** client queries registry (Consul, Eureka), picks instance, calls directly
- **Server-side:** client calls LB; LB queries registry → simpler client

---

### 8. Blob / Object Storage (S3)
- Stores unstructured data (images, videos, backups) cheaply
- Globally unique key → byte stream
- Eventual consistency for overwrite/delete
- Use pre-signed URLs to let clients upload/download directly (bypass your server)

---

## PART 4 — Key Principles

### CAP Theorem
A distributed system can only guarantee 2 of 3 during a network partition:
- **C**onsistency: every read returns the most recent write
- **A**vailability: every request gets a response (possibly stale)
- **P**artition tolerance: system works despite network splits

In practice, network partitions happen → you choose **CP or AP**:
- CP: bank account balance, inventory count, distributed lock
- AP: social media feed, user profile, shopping cart

### Consistency Models (weakest → strongest)
```
Eventual       → writes propagate eventually; reads may be stale (Cassandra default)
Read-your-writes → you always see your own writes (important for UX)
Monotonic reads → once you see value X, you won't see older value Y
Causal         → causally related writes seen in order
Strong / Linearizability → all reads see the latest write (expensive, requires quorum)
```

### Replication
- **Leader-follower (primary-replica):** single writer, multiple readers; failover needed
- **Multi-leader:** multiple writers (active-active); conflict resolution required
- **Leaderless (Dynamo-style):** quorum reads/writes (W+R>N); highly available

### PACELC
Extends CAP: even when no partition (Else), there's a Latency vs Consistency trade-off.
- DynamoDB: PA/EL (available + low latency)
- Zookeeper: PC/EC (consistent, higher latency)

---

## PART 5 — System Designs

---

### Design 1: Rate Limiter

**Requirements**
- Limit N requests per user per window (e.g., 100 req/min)
- Distributed (multiple API servers sharing state)
- Low overhead (<1ms added latency)
- Graceful degradation (allow if limiter is down)

**Algorithms**

| Algorithm | Pros | Cons |
|-----------|------|------|
| Fixed Window | Simple, O(1) | Burst at window boundary (2× rate) |
| Sliding Window Log | Accurate | High memory (log every request) |
| Sliding Window Counter | Accurate, O(1) | Slight approximation |
| Token Bucket | Allows bursts, smooth | Slightly complex |
| Leaky Bucket | Smooth output | Drops burst traffic |

**Token Bucket (Uber's choice):**
```
bucket: capacity=100, rate=10 tokens/sec
on request:
  tokens += (now - last_refill) * rate   ← refill
  last_refill = now
  if tokens >= 1:
    tokens -= 1; allow
  else:
    reject (429)
```

**Architecture**
```
Client → API Gateway → [Rate Limiter middleware]
                            ↓
                         Redis
                    INCR key; EXPIRE key
                    (Lua script = atomic)
```

**Redis Lua script (atomic check-and-decrement):**
```lua
local current = redis.call('GET', KEYS[1])
if current and tonumber(current) >= tonumber(ARGV[1]) then
  return 0  -- reject
else
  redis.call('INCR', KEYS[1])
  redis.call('EXPIRE', KEYS[1], ARGV[2])
  return 1  -- allow
end
```

**Key design:** `rate:{userId}:{window}` → counter with TTL = window size.

**Trade-offs:**
- Redis single node → fast but SPOF → use Redis Sentinel or Redis Cluster
- If Redis is down → fail open (allow all) or fail closed (reject all) → usually fail open
- Multi-region: each region has its own Redis → may slightly exceed global limit (acceptable)

---

### Design 2: URL Shortener (TinyURL)

**Requirements**
- Shorten URL: `POST /shorten` → returns short code
- Redirect: `GET /{code}` → 301/302 redirect to original URL
- 100M new URLs/day; 10B total; 10:1 read/write ratio
- Short URL must be unique, 6-8 characters

**Scale estimates**
```
Writes:  100M / 86400 ≈ 1,200 QPS
Reads:   12,000 QPS
Storage: 10B * 500 bytes (URL) ≈ 5 TB
```

**Core Algorithm: Base62 encoding**
```
Characters: [0-9][a-z][A-Z] → 62 chars
6 chars → 62^6 ≈ 56 billion combinations (enough)

Approach 1 — Hash + truncate:
  code = base62(md5(longURL))[0:7]
  collision? → append counter, re-hash

Approach 2 — Auto-increment ID:
  id = DB.nextId()
  code = base62(id)          ← no collision, predictable
```

**Data Model**
```sql
urls (
  code        VARCHAR(8) PRIMARY KEY,
  long_url    TEXT NOT NULL,
  user_id     BIGINT,
  created_at  TIMESTAMP,
  expires_at  TIMESTAMP
)
```

**Architecture**
```
Client → LB → API servers → Cache (Redis, code→URL, TTL=24h)
                          ↓ (cache miss)
                          DB (MySQL, sharded by code)
```

**Read path:** Check Redis → hit: 302 redirect. Miss: query DB → populate cache → redirect.

**301 vs 302:**
- 301 (permanent): browser caches → reduces server load but can't update destination
- 302 (temporary): every request hits server → can track clicks, update destination

**Sharding:** hash(code) % N → consistent hash for easy node addition.

---

### Design 3: Uber Ride Dispatch System

**Requirements**
- Rider requests a ride → match to nearest available driver within ~15 sec
- Real-time driver location updates (every 4 sec)
- ETA estimation, fare estimation
- 10M rides/day, 1M concurrent drivers

**Scale**
```
Location updates: 1M drivers × 1 update/4s = 250,000 writes/sec
Ride requests:    10M/day = ~120 QPS (peak ~1000 QPS)
```

**High-Level Architecture**
```
Driver App → Location Service → Kafka → Location Store (Redis GEO / H3)
Rider App  → Dispatch Service → Matching Engine → Driver App (WebSocket)
                              → Supply Service
                              → ETA Service
```

**Key Components**

**1. Location Service**
- Drivers send GPS coordinates every 4 seconds via WebSocket
- Location Service writes to Kafka topic `driver-locations`
- Consumer updates Redis GEO or H3 hexagon store
```
Redis GEO:
  GEOADD drivers <lng> <lat> <driver_id>
  GEORADIUSBYMEMBER drivers <lat> <lng> 2km ASC COUNT 20
```

**2. Geospatial Indexing: H3 Hexagonal Cells (Uber's actual approach)**
- World divided into hexagonal cells at multiple resolutions (0=continent, 15=~1m²)
- Resolution 9 cells: ~0.1 km² — good for dispatch
- Each driver stores their current H3 cell ID
- Search: find drivers in target cell + 6 neighboring cells (ring-1 expansion)
```
h3_index = h3.latLngToCell(lat, lng, resolution=9)
neighbors = h3.gridDisk(h3_index, k=1)   ← 7 cells total
available_drivers = Redis.SMEMBERS(f"cell:{neighbors[i]}")
```
- Advantages over geohash: equal-area hexagons, no edge distortion, clean ring queries

**3. Matching Engine**
```
For each ride request:
  1. Query available drivers in H3 ring-1 (expand to ring-2 if empty)
  2. For each candidate driver: compute ETA from Maps API
  3. Score drivers: score = ETA_weight × ETA + rating_weight × rating
  4. Offer trip to best driver (timeout 10s → next driver)
  5. Driver accepts → confirm booking
```

**4. Supply/Demand Service**
- Tracks supply (available drivers) and demand (pending requests) per H3 cell
- Used by surge pricing engine
- Publishes aggregates to Kafka every 30s

**Data Model**
```sql
trips (
  id          UUID PRIMARY KEY,
  rider_id    BIGINT,
  driver_id   BIGINT,
  status      ENUM(requested, accepted, in_progress, completed, cancelled),
  pickup_lat  DECIMAL(9,6),
  pickup_lng  DECIMAL(9,6),
  dest_lat    DECIMAL(9,6),
  dest_lng    DECIMAL(9,6),
  fare        DECIMAL(10,2),
  created_at  TIMESTAMP
)

driver_locations (Redis only — not DB)
  Key: "driver:{driver_id}"  Value: {lat, lng, h3_index, status, updated_at}
  TTL: 30s (stale if not updated)
```

**Trade-offs:**
- Redis for driver locations: fast O(1) lookup, but volatile → periodic DB checkpoints
- Kafka between location ingestion and storage: absorbs 250K/s spikes, decouples services
- WebSocket vs polling: WebSocket gives sub-second latency but requires connection management

---

### Design 4: Real-time Location Service (Geospatial)

**Requirements**
- Track 1M+ moving objects (drivers/couriers) in real-time
- Query: "find all objects within radius R of point P" in < 50ms
- Updates: each object sends location every 4 seconds
- Also used for: geofencing, surge zone detection, ETA

**Write path (250K updates/sec)**
```
Driver → WebSocket Server → Kafka (driver-locations topic)
                               ↓
                         Location Processor (consumer)
                               ↓
                         Redis GEO or H3 index
                         (+ async checkpoint to Cassandra)
```

**Query path**
```
Request: find drivers near (lat, lng, radius)
  1. Compute H3 cell for (lat, lng) at resolution 9
  2. Get ring-k neighbors (k=1 usually, expand if needed)
  3. Redis SUNION of driver sets for each cell → candidate list
  4. Filter by exact distance using Haversine formula
  5. Return top-N sorted by distance
```

**Geofencing (entry/exit detection)**
```
On each location update:
  new_cell = h3(lat, lng, resolution)
  old_cell = driver_state[driver_id].last_cell
  if new_cell != old_cell:
    publish "zone-transition" event to Kafka
    → downstream: surge recalculation, geofence alerts
```

**Persistent store (Cassandra)**
- Wide column: partition_key = h3_cell_id, clustering_key = driver_id
- Time-series location history for analytics, ETA model training
- Eventual consistency acceptable (historical data)

**Scaling WebSocket servers**
- Sticky load balancing (driver always connects to same WS server)
- WS servers are stateless re: location — they just forward to Kafka
- Horizontal scaling: add WS servers behind L4 LB

---

### Design 5: Surge Pricing Engine

**Requirements**
- Adjust price multiplier (1.0× – 5.0×) per geographic zone in near real-time
- Respond to supply/demand imbalances within 30–60 seconds
- Push new prices to all rider apps in the affected zone within 1 second

**Architecture**
```
Supply Service  (driver counts per H3 cell) ──┐
                                               ├─→ Kafka (supply-demand-events)
Demand Service  (ride request counts per cell) ┘       ↓
                                               Surge Calculator (stream processor)
                                                       ↓
                                               Surge Store (Redis, cell → multiplier)
                                                       ↓
                                               Push Service (WebSocket broadcast)
                                                       ↓
                                               Rider & Driver Apps
```

**Surge Calculation**
```
ratio = demand_requests / max(supply_drivers, 1)
if   ratio < 0.5:  multiplier = 1.0   (oversupply)
elif ratio < 1.0:  multiplier = 1.2
elif ratio < 1.5:  multiplier = 1.5
elif ratio < 2.0:  multiplier = 2.0
else:              multiplier = min(5.0, 1.0 + 0.5 * ratio)

Apply smoothing: new_mult = 0.7 * new + 0.3 * old  (exponential smoothing)
Store: Redis HSET surge:{h3_cell} multiplier 1.5 EX 120
```

**Push propagation**
- Surge Store change detected by push service (Redis keyspace notification or Kafka)
- Push service broadcasts to all WebSocket connections for riders in affected H3 cells
- Sub-second delivery achieved via persistent WebSocket connections

**Behavioral impact (from Uber research):** drivers self-position toward surged areas by 10-60%, which reduces the surge faster → self-correcting feedback loop.

**Trade-offs:**
- Surge granularity: too fine (cell too small) → noisy; too coarse → unfair across zones
- Update frequency: every 30s balances responsiveness vs system load
- Price rounding: round to nearest 0.1× for UX

---

### Design 6: Notification Service

**Requirements**
- Send Push (FCM/APNs), Email (SendGrid), SMS (Twilio)
- Guarantee at-least-once delivery
- Priority lanes: critical (OTP, trip update) vs marketing
- 10M notifications/day, peak 100K/min

**Architecture**
```
Event Sources (Trip, Payment, Marketing)
    ↓
Event Bus (Kafka)
    ├── high-priority topic
    └── low-priority topic
         ↓
    Notification Service (consumers)
         ↓
    [Preference DB]  →  Filter: user opted out? wrong channel?
         ↓
    Channel Routers
    ├── Push Worker → FCM/APNs
    ├── Email Worker → SendGrid
    └── SMS Worker → Twilio
         ↓
    Delivery Status → DB (for retry)
```

**Retry with exponential backoff**
```
attempt 1: immediate
attempt 2: +30s
attempt 3: +2min
attempt 4: +10min
attempt 5: dead-letter queue → alert
```

**Data Model**
```sql
notifications (
  id              UUID PRIMARY KEY,
  user_id         BIGINT,
  channel         ENUM(push, email, sms),
  template_id     VARCHAR(64),
  payload         JSONB,
  status          ENUM(pending, sent, delivered, failed),
  attempts        INT DEFAULT 0,
  created_at      TIMESTAMP,
  sent_at         TIMESTAMP
)

user_preferences (
  user_id         BIGINT,
  channel         VARCHAR(16),
  enabled         BOOLEAN,
  device_token    TEXT          -- for push
)
```

**Fan-out for bulk notifications (marketing):**
- Don't send 10M notifications synchronously
- Batch job: reads user segments → publishes to Kafka in chunks of 10K
- Workers consume in parallel → horizontal scaling

**Trade-offs:**
- At-least-once with idempotency key on push providers (FCM has message ID)
- Prefer Kafka over RabbitMQ for fan-out to multiple consumers (audit, analytics, delivery)
- Email has highest deliverability risk → dedicated IP, domain warmup

---

### Design 7: Distributed Cache (Deep Dive)

**Requirements**
- Sub-millisecond reads for hot data
- Handle 1M QPS reads, 100K QPS writes
- 100 GB of cached data
- High availability (< 1 min downtime on node failure)

**Redis Architecture**
```
Clients → Redis Cluster (16,384 hash slots)
          ├── Shard 0 (slots 0–5460):    Primary + 2 Replicas
          ├── Shard 1 (slots 5461–10922):Primary + 2 Replicas
          └── Shard 2 (slots 10923–16383):Primary + 2 Replicas
```
- **Key routing:** `slot = CRC16(key) % 16384` → maps to shard automatically
- **Replication:** async (fast) or semi-sync (`WAIT` command for stronger guarantee)
- **Failover:** Redis Sentinel or Redis Cluster auto-failover (<30s)

**Cache-Aside Pattern (most common)**
```
function get(key):
  val = redis.GET(key)
  if val: return val
  val = db.query(key)
  redis.SET(key, val, EX=3600)
  return val

function write(key, val):
  db.write(key, val)
  redis.DEL(key)   ← invalidate, NOT update (avoids race condition)
```

**Thundering herd (cache stampede):** cache expires → 1000 requests all miss → DB overloaded
Solutions:
1. Probabilistic early expiry: re-fetch slightly before TTL expires
2. Mutex lock: first miss acquires lock; others wait/serve stale
3. Background refresh: async job refreshes before TTL

**Hot key problem:** one key gets 10x normal traffic
Solutions:
1. Local in-process cache (LRU) in each app server (read from local first)
2. Key splitting: `hot_key_{server_id}` → distribute across replicas
3. Jittered TTL: add random ±10% to TTL to spread expiry

---

### Design 8: News Feed (Twitter / Facebook)

**Requirements**
- Post: user creates content
- Feed: user sees posts from people they follow, reverse-chronological
- 100M DAU, average 200 followers, celebrities up to 10M followers
- Feed load < 200ms

**The Core Trade-off: Fan-out on Write vs Fan-out on Read**

| | Fan-out on Write (push) | Fan-out on Read (pull) |
|--|------------------------|------------------------|
| How | On post: write to all followers' feed cache | On feed load: fetch from all followees, merge |
| Read | O(1) — pre-computed | O(N followees) — expensive |
| Write | O(N followers) — expensive for celebrities | O(1) |
| Staleness | Fresh immediately | Slight delay |
| Best for | Normal users (≤5K followers) | Celebrities (millions of followers) |

**Hybrid Approach (real-world solution)**
```
if poster.followers < 5000:
    fan-out on write → push post to all followers' feed cache
else (celebrity):
    fan-out on read  → pull at read time, merge with pre-computed feed
```

**Architecture**
```
POST /post
  → Post Service → DB (posts table)
  → Fanout Service (Kafka consumer)
       ├── Normal user: write post_id to feed cache of each follower (Redis List)
       └── Celebrity: skip fan-out; flag for read-time fetch

GET /feed
  → Feed Service
       ├── Read pre-computed feed from Redis (post_ids)
       ├── Fetch celebrity posts from followee post indexes
       ├── Merge + deduplicate + sort by timestamp
       └── Hydrate post details (post content, author info)
```

**Data Model**
```sql
posts (id, author_id, content, media_url, created_at)
follows (follower_id, followee_id, created_at)

-- Redis
user_feed:{user_id} → Sorted Set, score=timestamp, member=post_id
  ZADD user_feed:123 1700000000 post_456
  ZREVRANGE user_feed:123 0 49  ← get latest 50 posts
```

**Pagination:** use cursor (last seen post_id + timestamp) instead of OFFSET — avoids re-scanning.

---

## PART 6 — Common Trade-off Cheat Sheet

| Decision | Option A | Option B | Choose A when... |
|----------|----------|----------|-----------------|
| SQL vs NoSQL | SQL | NoSQL | Need ACID, complex queries, joins |
| Cache invalidation | TTL | Event-driven | Write frequency is low |
| Fan-out | Write-time | Read-time | Most users have few followers |
| Consistency | Strong | Eventual | Money/inventory involved |
| Transport | HTTP REST | WebSocket | Need real-time bidirectional |
| Queue | Kafka | RabbitMQ | Need fan-out, replay, high throughput |
| Sync vs Async | Sync | Async (Kafka) | Non-critical path, decoupling needed |
| Storage | SQL DB | Blob (S3) | Unstructured data, large files |
| Location index | Geohash | H3 (Uber) | Need uniform cell area, ring queries |

---

## PART 7 — Uber-Specific Cheat Sheet

| Question | Answer |
|----------|--------|
| Geospatial indexing | H3 hexagonal cells (not geohash) |
| Location updates | WebSocket + Kafka pipeline |
| Location storage | Redis (hot) + Cassandra (cold) |
| Ride matching | H3 ring expansion + ETA scoring |
| Surge pricing | Supply/demand per H3 cell, event-driven, WebSocket push |
| Microservices | 2200+ services (as of 2019), Kafka as backbone |
| Real-time updates | WebSocket for sub-second delivery |
| Driver positioning | Drivers move toward surge zones (10–60% increase) |

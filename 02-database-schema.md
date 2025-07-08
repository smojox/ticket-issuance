# Database Schema - SQLite Local Storage

## Overview
The application uses SQLite database with Room persistence library for offline-first architecture. All data is stored locally with sync capabilities for future API integration.

## Database Tables

### 1. Users Table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT,
    badge_number TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

**Sample Data:**
```sql
INSERT INTO users (username, password, full_name, badge_number, created_at, updated_at) 
VALUES ('Test', 'Test', 'Test Officer', 'CEO001', datetime('now'), datetime('now'));
```

### 2. Streets Table
```sql
CREATE TABLE streets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    area_code TEXT,
    is_active INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

**Sample Data (10 Streets):**
```sql
INSERT INTO streets (name, area_code, is_active, created_at, updated_at) VALUES
('High Street', 'HS01', 1, datetime('now'), datetime('now')),
('Market Square', 'MS01', 1, datetime('now'), datetime('now')),
('Victoria Road', 'VR01', 1, datetime('now'), datetime('now')),
('Church Lane', 'CL01', 1, datetime('now'), datetime('now')),
('King Street', 'KS01', 1, datetime('now'), datetime('now')),
('Queen Avenue', 'QA01', 1, datetime('now'), datetime('now')),
('Mill Road', 'MR01', 1, datetime('now'), datetime('now')),
('Station Road', 'SR01', 1, datetime('now'), datetime('now')),
('Park Street', 'PS01', 1, datetime('now'), datetime('now')),
('Castle Street', 'CS01', 1, datetime('now'), datetime('now'));
```

### 3. Contraventions Table
```sql
CREATE TABLE contraventions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    description TEXT NOT NULL,
    observation_time_minutes INTEGER NOT NULL,
    penalty_amount REAL NOT NULL,
    street_id INTEGER,
    is_active INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (street_id) REFERENCES streets(id)
);
```

**Sample Data (10 contraventions per street):**
```sql
-- Contraventions for each street (example for High Street)
INSERT INTO contraventions (code, description, observation_time_minutes, penalty_amount, street_id, is_active, created_at, updated_at) VALUES
('01', 'Parked in a restricted street during prescribed hours', 5, 70.00, 1, 1, datetime('now'), datetime('now')),
('02', 'Parked or loading/unloading in a restricted street where waiting and loading/unloading restrictions are in force', 5, 70.00, 1, 1, datetime('now'), datetime('now')),
('06', 'Parked without clearly displaying a valid pay & display ticket or voucher', 0, 70.00, 1, 1, datetime('now'), datetime('now')),
('07', 'Parked with payment made to extend the stay beyond initial time', 0, 70.00, 1, 1, datetime('now'), datetime('now')),
('12', 'Parked in a residents or shared use parking place or zone without a valid permit', 0, 70.00, 1, 1, datetime('now'), datetime('now')),
('16', 'Parked in a permit space or zone without displaying a valid permit', 0, 70.00, 1, 1, datetime('now'), datetime('now')),
('19', 'Parked in a residents or shared use parking place or zone displaying an invalid permit', 0, 70.00, 1, 1, datetime('now'), datetime('now')),
('23', 'Parked in a parking place or area not designated for that class of vehicle', 0, 70.00, 1, 1, datetime('now'), datetime('now')),
('25', 'Parked in a loading place or bay during restricted hours without reasonable excuse', 5, 70.00, 1, 1, datetime('now'), datetime('now')),
('30', 'Parked for longer than permitted', 10, 70.00, 1, 1, datetime('now'), datetime('now'));
```

### 4. Vehicle Makes Table
```sql
CREATE TABLE vehicle_makes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    is_active INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

**Sample Data (5 Makes):**
```sql
INSERT INTO vehicle_makes (name, is_active, created_at, updated_at) VALUES
('Ford', 1, datetime('now'), datetime('now')),
('Volkswagen', 1, datetime('now'), datetime('now')),
('BMW', 1, datetime('now'), datetime('now')),
('Mercedes', 1, datetime('now'), datetime('now')),
('Toyota', 1, datetime('now'), datetime('now'));
```

### 5. Vehicle Models Table
```sql
CREATE TABLE vehicle_models (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    make_id INTEGER NOT NULL,
    is_active INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (make_id) REFERENCES vehicle_makes(id)
);
```

**Sample Data (5 Models per Make):**
```sql
-- Ford Models
INSERT INTO vehicle_models (name, make_id, is_active, created_at, updated_at) VALUES
('Focus', 1, 1, datetime('now'), datetime('now')),
('Fiesta', 1, 1, datetime('now'), datetime('now')),
('Mondeo', 1, 1, datetime('now'), datetime('now')),
('Kuga', 1, 1, datetime('now'), datetime('now')),
('Transit', 1, 1, datetime('now'), datetime('now'));

-- Similar entries for other makes...
```

### 6. Observations Table
```sql
CREATE TABLE observations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vrm TEXT NOT NULL,
    street_id INTEGER NOT NULL,
    contravention_id INTEGER NOT NULL,
    make_id INTEGER NOT NULL,
    model_id INTEGER NOT NULL,
    valve_position_front INTEGER, -- 1-12 clock positions
    valve_position_rear INTEGER,  -- 1-12 clock positions
    observation_start_time TEXT NOT NULL,
    observation_end_time TEXT,
    status TEXT DEFAULT 'active', -- active, completed, cancelled
    user_id INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (street_id) REFERENCES streets(id),
    FOREIGN KEY (contravention_id) REFERENCES contraventions(id),
    FOREIGN KEY (make_id) REFERENCES vehicle_makes(id),
    FOREIGN KEY (model_id) REFERENCES vehicle_models(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 7. Tickets Table
```sql
CREATE TABLE tickets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_number TEXT UNIQUE NOT NULL,
    vrm TEXT NOT NULL,
    street_id INTEGER NOT NULL,
    contravention_id INTEGER NOT NULL,
    make_id INTEGER NOT NULL,
    model_id INTEGER NOT NULL,
    valve_position_front INTEGER,
    valve_position_rear INTEGER,
    observation_id INTEGER, -- NULL if issued directly
    issue_time TEXT NOT NULL,
    penalty_amount REAL NOT NULL,
    status TEXT DEFAULT 'issued', -- issued, paid, cancelled
    user_id INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (street_id) REFERENCES streets(id),
    FOREIGN KEY (contravention_id) REFERENCES contraventions(id),
    FOREIGN KEY (make_id) REFERENCES vehicle_makes(id),
    FOREIGN KEY (model_id) REFERENCES vehicle_models(id),
    FOREIGN KEY (observation_id) REFERENCES observations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 8. Sync Queue Table
```sql
CREATE TABLE sync_queue (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    operation_type TEXT NOT NULL, -- 'upload_ticket', 'upload_observation', 'download_updates'
    table_name TEXT NOT NULL,
    record_id INTEGER,
    data_json TEXT, -- JSON representation of data to sync
    status TEXT DEFAULT 'pending', -- pending, in_progress, completed, failed
    retry_count INTEGER DEFAULT 0,
    last_attempt_time TEXT,
    error_message TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

## Database Relationships
- **Users** → **Observations** (One-to-Many)
- **Users** → **Tickets** (One-to-Many)
- **Streets** → **Contraventions** (One-to-Many)
- **Streets** → **Observations** (One-to-Many)
- **Streets** → **Tickets** (One-to-Many)
- **Vehicle Makes** → **Vehicle Models** (One-to-Many)
- **Vehicle Makes** → **Observations** (One-to-Many)
- **Vehicle Models** → **Observations** (One-to-Many)
- **Observations** → **Tickets** (One-to-One, Optional)

## Indexes for Performance
```sql
CREATE INDEX idx_observations_vrm ON observations(vrm);
CREATE INDEX idx_observations_status ON observations(status);
CREATE INDEX idx_tickets_vrm ON tickets(vrm);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_sync_queue_status ON sync_queue(status);
CREATE INDEX idx_contraventions_street ON contraventions(street_id);
CREATE INDEX idx_vehicle_models_make ON vehicle_models(make_id);
```

## Room Entity Annotations
Each table will be implemented as a Room entity with appropriate annotations:
- `@Entity` for table definition
- `@PrimaryKey` for primary keys
- `@ForeignKey` for foreign key relationships
- `@ColumnInfo` for column specifications
- `@Index` for database indexes
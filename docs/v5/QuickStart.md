# Bond Future Quote Extractor QuickStart

## Overview
The Bond Future Quote Extractor extracts market data from the `bond.fut_tick` MySQL table and converts it into the standardized `BondFutureQuoteDataModel`.

## Prerequisites
- **Java**: JDK 1.8+
- **Database**: Access to MySQL database containing `bond.fut_tick` table.
- **Dependencies**: `commons-dbcp2`, `mysql-connector-java` (added to pom.xml).

## Configuration
Add the following to your `etl.ini` file:

```ini
[source:bond_future_quote]
name = bond-future-quote-db
type = database
category = BondFutureQuote
connectionString = jdbc:mysql://host:3306/bond

# Database Credentials
db.url = jdbc:mysql://host:3306/bond
db.user = <your_user>
db.password = <your_password>

# Pool Settings
db.pool.min = 1
db.pool.max = 5
db.timeout.seconds = 300

# Query Template
sql.template = select * from bond.fut_tick where trading_date = {BUSINESS_DATE}
```

## Usage

### CLI Execution
Run the extractor via standard ETL command line (assuming CLI support exists for generic sources):

```bash
java -jar etl-tool.jar -source bond-future-quote-db -date 20231201
```

### Developer Usage
To use the extractor programmatically:

```java
// 1. Setup Context
ETLContext context = new ETLContext();
context.setCurrentDate(LocalDate.of(2023, 12, 1));
ETConfiguration config = ...; // load config
context.setConfig(config);

// 2. Create Extractor (via Factory)
ETConfiguration.SourceConfig sourceConfig = config.findSourceConfig("bond-future-quote-db");
Extractor extractor = ExtractorFactory.createExtractor(sourceConfig);

// 3. Extract
extractor.setup(context);
List<SourceDataModel> data = extractor.extract(context);

// 4. Process...
```

## Component Architecture
- **BondFutureQuoteDataModel**: POJO representing the quote.
- **BondFutureQuoteExtractor**: Concrete extractor logic.
- **DatabaseExtractor**: Base class handling connection pooling and retry.
- **DatabaseConnectionManager**: Singleton wrapper for DBCP2 DataSource.

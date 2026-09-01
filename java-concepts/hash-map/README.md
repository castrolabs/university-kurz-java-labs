# The HashMap Class

## Goal

Store and update key/value pairs with `HashMap`, read `put()`'s return value to detect whether a call was an insert or an update, and iterate entries with `entrySet()` and `Map.Entry` when you need both the key and the value at once.

## Prerequisites

- Basic Java syntax
- Familiarity with `Optional`

## Task

`PriceCatalog` keeps a `HashMap<String, Double>` mapping product names to prices. You'll implement setting a price (while reporting the previous one), looking up a price, applying a discount by reading the current price and writing the discounted one back, and finding the most expensive product by scanning the map's entries.

## Instructions

Complete the following TODOs in `PriceCatalog`:

- TODO-00: Implement `setPrice()`, storing the price and returning the previous one (or empty, if the product was new).
- TODO-01: Implement `getPrice()`, returning the price for a product, or empty if it isn't in the catalog.
- TODO-02: Implement `applyDiscount()` — read the current price, compute the discounted price, and put it back.
- TODO-03: Implement `mostExpensive()` using `entrySet()` and `Map.Entry` to compare products by price.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/hash-map test
```

Or from the lab directory:

```bash
cd java-concepts/hash-map
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Implement `totalValue()`, summing every price in the catalog via `entrySet()`.

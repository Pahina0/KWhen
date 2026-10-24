# KWhen

**KWhen** is a lightweight, zero-dependency natural language processing (NLP) library for **Kotlin Multiplatform (KMP)** that detects, extracts, and normalizes date, time, duration, and recurring temporal expressions from English sentences into strongly typed models.

[![GitHub Release](https://img.shields.io/github/v/release/pahina0/kwhen)](https://github.com/pahina0/kwhen/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.pahinaa.kwhen/kwhen)](https://central.sonatype.com/artifact/io.github.pahinaa.kwhen/kwhen)
[![License](https://img.shields.io/github/license/pahina0/kwhen)](LICENSE)

---

## Supported Platforms

* **JVM**: Desktop, Server, Backend runtimes (Java 11+)
* **Android**: API 24+ (`debug` and `release` AAR variants)
* **Apple / iOS**: `iosArm64`, `iosSimulatorArm64`, `iosX64`
* **Linux**: `linuxX64`
* **Windows**: `mingwX64`

---

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.pahinaa.kwhen:kwhen:<version>")
}
```

Or download artifacts directly from [Maven Central](https://repo1.maven.org/maven2/io/github/pahinaa/kwhen/kwhen/).

---

## Features

* **Specific & Relative Dates**: `"today"`, `"tomorrow"`, `"yesterday"`, `"tmrw"`, `"2moro"`, `"on July 4th"`, `"on 22nd May 2026"`, `"on the 3rd of March, 2027"`, `"09/25/2025"`.
* **European (`DD/MM/YYYY`) & US (`MM/DD/YYYY`) Formats**: Configurable day/month precedence via `useEuropeanDate`.
* **Times & Time-of-Day Disambiguation**: 12-hour and 24-hour clock formats (`"at 9am"`, `"at 3:15pm"`, `"at 9:00 a.m."`, `"at 14:30"`, `"at noon"`, `"midnight"`, `"in the morning"`, `"at 8am in the morning"`, `"at 7 in the evening"`).
* **Ranges & Intervals**:
  * Clock ranges: `"from 8am to 4pm"`, `"between 7pm and 11pm"`, overnight `"10pm to 2am"`.
  * Weekday intervals: `"from Monday to Friday"`, `"starting Monday until Friday"`.
  * Durations (natural & inverted): `"tomorrow at 3pm for 2 hours"`, `"for 3 days starting June 3rd"`.
* **Recurring Schedules**: `"daily at 9am"`, `"every other Tuesday"`, `"biweekly"`, `"every month on the 1st"`, `"every July 4th"`, `"every quarter year"`.
* **Relative Calculations**: `"in 45 mins"`, `"in 2 weeks"`, `"3 days from Monday"`, `"15 minutes after 4pm"`.
* **Spelled-Out Numbers**: Words like `"three hours"`, `"twenty-five minutes"`, `"half an hour"`, `"quarter of a day"`.
* **Distractor Resilience**: Filters non-temporal numbers (e.g. `"room 401"`, `"table for 2"`) and indefinite articles (`"a"` / `"an"`).

---

## Quick Start

Create an instance of `TimeParser` and pass any text to `.parse()`:

```kotlin
import ap.panini.kwhen.TimeParser

val tp = TimeParser()
val results = tp.parse("I will go to the gym tomorrow at 6pm")

println(results.first().startTime.first()) // 2025-06-12T18:00
```

---

## Configuration

Customize timezone, reference epoch, time-of-day thresholds, and date format conventions using `ENConfig`:

```kotlin
import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.configs.ENConfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

val config = ENConfig(
    // Reference base time (defaults to Clock.System.now())
    relativeTo = LocalDateTime(2025, 6, 11, 8, 0, 0)
        .toInstant(TimeZone.UTC)
        .toEpochMilliseconds(),

    // Timezone used for relative interval and calendar calculations
    timeZone = TimeZone.of("Europe/London"),

    // Interpret ambiguous numeric dates as DD/MM/YYYY (default is false: MM/DD/YYYY)
    useEuropeanDate = true,

    // Clock mode: false = 12-hour rollover heuristics, true = strict 24-hour mode
    use24 = false,

    // Default hour values for vague periods
    morning = 9,
    afternoon = 15,
    evening = 18,
    night = 20
)

val parser = TimeParser(config)
val parsed = parser.parse("Conference runs on 05/08 at 14:00") // Interpreted as August 5th
```

---

## Parsed Output Model

Each extracted expression returns a `Parsed` data object:

```kotlin
val results = parser.parse("Johnny has workshop from Monday to Friday from 9am to 5pm")
val match = results.first()

match.text           // Extracted substring: "from Monday to Friday from 9am to 5pm"
match.range          // IntRange: character bounds in original string
match.startTime      // List<LocalDateTime>: resolved start date/times
match.endTime        // LocalDateTime?: resolved boundary end time
match.tagsTimeStart  // Set<TimeUnit>: [WEEK, DAY, HOUR, MINUTE]
match.tagsTimeEnd    // Set<TimeUnit>: [WEEK, DAY, HOUR, MINUTE]
match.repeatTag      // TimeUnit?: recurrence unit (e.g. DAY, WEEK, MONTH, YEAR)
match.repeatOften    // Double?: recurrence frequency multiplier
```

### Recurrence Example

```kotlin
val parsed = parser.parse("Sprint planning is every other Tuesday at 10am")
val item = parsed.first()

println(item.repeatTag)   // TimeUnit.WEEK
println(item.repeatOften) // 2.0 (every 2 weeks)
```

---

## Quality & Verification

KWhen is verified across platforms with comprehensive multiplatform test suites in `commonTest`:
* **Combinatorial Grammar Suite**: Evaluates over 2,200 valid English combinations of dates, times, prepositions, ranges, and distractors.
* **Metamorphic & Sweep Suite**: Verifies relational invariants across all 24 anchor hours, leap-year boundaries, month rollovers, Daylight Saving Time (DST) transitions, and casing/padding transformations.
* **Seeded Fuzz & Boundary Suite**: Fuzzes adversarial numeric, boundary, and Unicode text to ensure zero crashes and strict substring/range invariants.

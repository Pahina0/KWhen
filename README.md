# KWhen

**KWhen** is a natural language processor for **Kotlin Multiplatform** that extracts time-related expressions from English sentences.

---

## Download

[![GitHub Release](https://img.shields.io/github/v/release/pahina0/kwhen)](https://github.com/pahina0/kwhen/releases)

Add the dependency using **Gradle**. Replace `<version>` with the [latest version](https://central.sonatype.com/artifact/io.github.pahinaa.kwhen/kwhen/overview):

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.pahinaa.kwhen:kwhen:<version>")
}
```

You can also download the `.jar` directly from [Maven Central](https://repo1.maven.org/maven2/io/github/pahinaa/kwhen/kwhen/).

---

## Usage

To use the processor, create an instance of `TimeParser`. An optional configuration (`Config`) can be provided to customize interpretation. Currently, only English (`ENConfig`) is supported.

```kotlin
val tp = TimeParser()
tp.parse("I will go to the gym tmrw at 6")
```

### Custom Configuration

You can customize the meaning of ambiguous phrases using `ENConfig`:

```kotlin
val tp2 = TimeParser(
    ENConfig(
        evening = 18,
        morning = 9,
        afternoon = 15,
        night = 20,
        use24 = false
    )
)
tp2.parse("He goes swimming in the morning")
```
---

## Parsed Output

A call to `parse()` returns a list of `Parsed` objects. Each object contains:

```kotlin
val tp = TimeParser()
val parsed = tp.parse("Johnny has school from 8am to 4pm")
val firstParsed = parsed[0]

firstParsed.text            // "from 8am to 4pm"
firstParsed.range           // IntRange: (17..32)
firstParsed.startTime       // List<LocalDateTime>: [2024-06-25T08:00]
firstParsed.endTime         // LocalDateTime: 2024-06-25T16:00]
firstParsed.tagsTimeStart   // Set<TimeTag>: [HOUR, MINUTE]
firstParsed.tagsTimeEnd     // Set<TimeTag>: [HOUR, MINUTE]
```

### Repeating Time Example

```kotlin
val parsed = tp.parse("It's a holiday every Jul 4th")
val firstParsed = parsed[0]

firstParsed.repeatTag    // TimeUnit.YEAR
firstParsed.repeatOften  // 1
```

If multiple unrelated or detached time expressions are found, each is returned as a separate `Parsed` object in the result list.

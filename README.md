# KWhen

KWhen is a natural language processor that can extract phrases in sentences related to time, written for Kotlin Multiplatform.

## Download

![GitHub Release](https://img.shields.io/github/v/release/pahina0/kwhen)  
You can include the project using Gradle, replacing the version with the latest version found [here](https://central.sonatype.com/artifact/io.github.pahinaa.kwhen/kwhen/overview):

```gradle
repositories {
  mavenCentral()
}

dependencies {
    implementation("io.github.pahinaa.kwhen:kwhen:<version>")
}
```

You can also download the jar directly from [here](https://repo1.maven.org/maven2/io/github/pahinaa/kwhen/kwhen/)

## Usage

To use the processor, it requires an instance of `TimeParser` and an optional `Config` for language you want. The only currently supported language is English which uses `ENConfig`.

```kotlin
val tp = TimeParser()
tp.parse("I will go to the gym tmrw at 6")

/**
 * The following are possible configuration for english. 
 * Changing these values change how various ambiguous values can be interpreted
 */
val tp2 = TimeParser(
            ENConfig(
                evening = 17,
                morning = 4,
                afternoon = 12,
                night = 19,
                use24 = true
            )
        )
tp2.parse("He goes swimming in the morning")
// morning will now be considered as 4am

/**
 * Defaults for english
 * evening = 18
 * morning = 9
 * afternoon = 15
 * night = 20
 * use24 = false
 */
```

### Parsed Value

When parsing, a list of `Parsed` are returned.

```kotlin
val tp = TimeParser()
var parsed = tp.parse("Johnny has school from 8am to 4pm")

//The returned parsed object contains the following
var firstParsed = parsed[0]
firstParsed.text // the text that was extracted ( from 8am to 4pm)
firstParsed.range // the range the extracted text was found from (17..32)
firstParsed.startTime // a list of estimated time at which the time in the sentence is as a kotlinx-datetime object. ([2024-06-25T08:00])
firstParsed.endTime // the end time which can be found in the sentence as a kotlinx-datetime object. (2024-06-25T16:00)
firstParsed.tagsTimeStart // the list of time units which are definite for the starting time ([HOUR, MINUTE]) as from 8am, it knows the hour is 8 and minute is 0
firstParsed.tagsTimeEnd // the list of time units which are definite for the ending time ([HOUR, MINUTE]) as from 4pm, it knows the hour is 16 and minute is 0

// repeating times
parsed = tp.parse("Its a holiday every Jul 4th")
firstParsed = parsed[0]
firstParsed.repeatTag // the time units where it repeats (YEAR)
firstParsed.repeatOften // how often the time unit repeats (1)
// thus, it repeats every 1 year
```

If multiple times are found uncorrelated to each other or detatched from each other in the sentence, they will be seperate entries in the list of `Parsed` objects.

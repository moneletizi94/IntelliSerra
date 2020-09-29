# IntelliSerra
[![Build Status](https://travis-ci.org/moneletizi94/IntelliSerra.svg?branch=develop)](https://travis-ci.org/moneletizi94/IntelliSerra)
[![codecov.io](https://codecov.io/github/moneletizi94/IntelliSerra/coverage.svg?branch=develop)](https://codecov.io/github/moneletizi94/IntelliSerra?branch=develop)



### Tech
Main technologies used are:
* [Akka] - the system is built on top of akka actors
* [TuProlog] - used as engine for rules inference

## Installation
You must download the library from "Releases" section and import it in your local maven repository since it is not published.

## Usage

### Server
1. In order to define new Actions and Categories you must extend it.
```scala
// Some custom categories
case object Humidity extends Category[DoubleType]
case object AirTemperature extends Category[DoubleType]
...
// Some custom actions
final case class Water(override val time: FiniteDuration) extends TimedAction
final case class Dehumidifies(override val switchStatus: Boolean) extends ToggledAction
...
```

2. You must define an aggregation logic for each category created
```scala
import AggregationFunctions._
val aggregators = List(
    createAggregator(AirTemperature)(avg),
    createAggregator(Humidity)(max)
)
```

3. You can define actuation rules
```scala
val rules = List(
    Humidity < 45.0 && AirTemperature < 40.0 execute Water(10 seconds),
    Humidity < 50.0 execute Dehumidifies(false),
    Humidity > 70.0 execute Dehumidifies(true)
    ...
)
```

4. Start your greenhouse server!
```scala
val config = ServerConfig(
    "greenhousename", "hostname", port = 8080,
    ZoneConfig(actionsEvaluationPeriod = ..., stateEvaluationPeriod = ..., aggregators),
    RuleConfig(rules)
)

// start the server
GreenHouseServer(config).start()
```

### Custom device
Define and deploy your actuators and sensors. For example:
```scala
val humidiySensor = Sensor("humidity1", Humidity, sensingPeriod = 5 seconds)
val dehumActuator = Actuator("dehum1", Dehumidifies.getClass) {
    case (state, Dehumidifies(true)) => ... // do something to real world
    case (state, Dehumidifies(false)) => ...
}

val deploy = DeviceDeploy("greenhousename", "hostname", port = 8080)
deploy.join(humiditySensor) // its async operation
deploy.join(dehumActuator)
```

### Interact using Client
```scala
val client = GreenhouseClient("greenhousename", "hostname", port = 8080)
for {
    _ <- client.createZone("zone1") // its async operation
    _ <- client.associateEntity("dehum1", "zone1")
}
// retrieve zone state client.getState("zone1") 
```

## License
Apache 2.0
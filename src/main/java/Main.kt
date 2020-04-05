import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


data class Point(val latitude: Float, val longitude: Float)
data class Participants(val passengers: Collection<Person>, val drivers: Collection<Person>)
data class Person(val id: UUID, val finishPoint: Point)

fun main() {
    val (passengers, drivers) = readPoints()
    val center = Point(59.981597f,30.214552f)
    for (passenger in passengers) {
        val suggestedDrivers = suggestDrivers(passenger, drivers, center)
        println("Passenger point: ${passenger.finishPoint.latitude}, ${passenger.finishPoint.longitude}")
        for (driver in suggestedDrivers) {
            println("  ${driver.finishPoint.latitude}, ${driver.finishPoint.longitude}")
        }
    }
}

fun dist(from: Point, to: Point): Float {
    return acos(sin(from.latitude) * sin(to.latitude)
            + cos(from.latitude) * cos(to.latitude) * cos(from.longitude - to.longitude)) * 6371.21f;
}

fun quality(passenger: Person, driver: Person, center: Point): Float {
    return (dist(center, driver.finishPoint) /
            (dist(center, passenger.finishPoint) + dist(passenger.finishPoint, driver.finishPoint)));
}

fun suggestDrivers(passenger: Person, drivers: Collection<Person>, center: Point): Collection<Person> {
    return drivers.sortedBy { quality(it, passenger, center) }.reversed();
}

private fun readPoints(): Participants {
    val pathToResource = Paths.get(Point::class.java.getResource("latlons").toURI())
    val allPoints = Files.readAllLines(pathToResource).map { asPoint(it) }.shuffled()
    val passengers = allPoints.slice(0..9).map { Person(UUID.randomUUID(), it) }
    val drivers = allPoints.slice(10..19).map { Person(UUID.randomUUID(), it) }
    return Participants(passengers, drivers)
}

private fun asPoint(it: String): Point {
    val (lat, lon) = it.split(", ")
    return Point(lat.toFloat(), lon.toFloat())
}



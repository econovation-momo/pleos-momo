package app.momo.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RolesTest {
    private val names = AppState().names

    @Test fun beltedRearSeatsGetRolesInOrder_driverListensUnlessJoins() {
        val seats = mapOf(
            SeatPos.DRIVER to SeatState(true, true),
            SeatPos.PASSENGER to SeatState(false, false),
            SeatPos.REAR_LEFT to SeatState(true, true),
            SeatPos.REAR_RIGHT to SeatState(true, true),
        )
        val p = assignRoles(SeedBots.dragon, seats, names, driverJoins = false)
        assertEquals(listOf("기사", "마법사", null), p.map { it.role })
        assertEquals("조연", assignRoles(SeedBots.dragon, seats, names, driverJoins = true).last().role)
    }

    @Test fun unbuckledRiderOnlyListens() {
        val seats = CarStateSeats(rearRightBelted = false)
        val p = assignRoles(SeedBots.dragon, seats, names, driverJoins = false)
        assertEquals(1, p.count { it.role != null })
    }

    private fun CarStateSeats(rearRightBelted: Boolean) = mapOf(
        SeatPos.REAR_LEFT to SeatState(true, true),
        SeatPos.REAR_RIGHT to SeatState(true, rearRightBelted),
    )
}

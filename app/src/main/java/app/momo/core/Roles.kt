package app.momo.core

/**
 * 좌석 착좌·벨트로 탑승자를 정하고 봇의 역할을 나눠 준다.
 * 뒷좌석(주로 아이) → 조수석 순서로 주연 역할을 주고, 운전자는 원할 때만 짧은 대사의 조연이 된다.
 * 착좌했지만 벨트를 안 맨 좌석은 '듣기만'으로 둔다.
 */
fun assignRoles(
    bot: StoryBot,
    seats: Map<SeatPos, SeatState>,
    names: Map<SeatPos, String>,
    driverJoins: Boolean,
): List<Player> {
    val order = listOf(SeatPos.REAR_LEFT, SeatPos.REAR_RIGHT, SeatPos.PASSENGER)
    var next = 0
    val players = order.mapNotNull { pos ->
        val s = seats[pos] ?: return@mapNotNull null
        if (!s.occupied) return@mapNotNull null
        val role = if (s.belted) bot.roles.getOrElse(next++) { "응원단" } else null
        Player(pos, names[pos] ?: pos.label, role)
    }
    val driver = seats[SeatPos.DRIVER]?.takeIf { it.occupied }?.let {
        Player(SeatPos.DRIVER, names[SeatPos.DRIVER] ?: "운전자", if (driverJoins) "조연" else null)
    }
    return players + listOfNotNull(driver)
}

val Player.plays get() = role != null

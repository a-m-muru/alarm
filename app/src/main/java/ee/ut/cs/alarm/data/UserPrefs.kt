package ee.ut.cs.alarm.data

import java.util.UUID
import ee.ut.cs.alarm.data.proto.UserPrefs as UserPrefsProto
import ee.ut.cs.alarm.data.proto.AlarmTrack as AlarmTrackProto
import ee.ut.cs.alarm.data.proto.AlarmGame as AlarmGameProto

enum class AlarmTrack(name: String) {
    ALARM_TRACK_1("Ringtone 1"),
    ALARM_TRACK_2("Ringtone 2"),
    ALARM_TRACK_3("Ringtone 3")
}

enum class AlarmGame(name: String) {
    ALARM_GAME_BALANCE_HOLE("Balance Hole"),
    ALARM_GAME_GO_INTO_THE_LIGHT("Go Into the Light"),
    ALARM_GAME_JUMPING_JACKS("Jumping Jacks")
}

data class UserPrefs(
    val id: UUID = UUID.randomUUID(),
    val allowedTracks: List<AlarmTrack> = listOf(),
    val allowedGames: List<AlarmGame> = listOf(),
    val streakCount: Int,
    val prevStreakCount: Int,
    val lastMinigameTs: Long
) {
    companion object {
        fun fromProto(proto: UserPrefsProto): UserPrefs {
            return UserPrefs(
                id = UUID.fromString(proto.id),
                allowedTracks = proto.allowedTracksList.map { value ->
                    when (value) {
                        AlarmTrackProto.ALARM_TRACK_1 -> AlarmTrack.ALARM_TRACK_1
                        AlarmTrackProto.ALARM_TRACK_2 -> AlarmTrack.ALARM_TRACK_2
                        AlarmTrackProto.ALARM_TRACK_3 -> AlarmTrack.ALARM_TRACK_3
                        AlarmTrackProto.UNRECOGNIZED -> AlarmTrack.ALARM_TRACK_1
                    }
                },
                allowedGames = proto.allowedGamesList.map { value ->
                    when (value) {
                        AlarmGameProto.ALARM_GAME_BALANCE_HOLE -> AlarmGame.ALARM_GAME_BALANCE_HOLE
                        AlarmGameProto.ALARM_GAME_GO_INTO_THE_LIGHT -> AlarmGame.ALARM_GAME_GO_INTO_THE_LIGHT
                        AlarmGameProto.ALARM_GAME_JUMPING_JACKS -> AlarmGame.ALARM_GAME_JUMPING_JACKS
                        AlarmGameProto.UNRECOGNIZED -> AlarmGame.ALARM_GAME_BALANCE_HOLE
                    }
                },
                streakCount = proto.streakCount,
                prevStreakCount = proto.prevStreakCount,
                lastMinigameTs = proto.lastMinigameTs
            )
        }
    }

    fun toProto(): UserPrefsProto {
        val builder = UserPrefsProto.newBuilder()
            .setId(id.toString())
            .setStreakCount(streakCount)
            .setPrevStreakCount(prevStreakCount)
            .setLastMinigameTs(lastMinigameTs)

        for (i in 0..<allowedTracks.size) {
            when (allowedTracks.get(i)) {
                AlarmTrack.ALARM_TRACK_1 -> builder.setAllowedTracks(i, AlarmTrackProto.ALARM_TRACK_1)
                AlarmTrack.ALARM_TRACK_2 -> builder.setAllowedTracks(i, AlarmTrackProto.ALARM_TRACK_2)
                AlarmTrack.ALARM_TRACK_3 -> builder.setAllowedTracks(i, AlarmTrackProto.ALARM_TRACK_3)
            }
        }

        for (i in 0..<allowedGames.size) {
            when (allowedGames.get(i)) {
                AlarmGame.ALARM_GAME_BALANCE_HOLE -> builder.setAllowedGames(i, AlarmGameProto.ALARM_GAME_BALANCE_HOLE)
                AlarmGame.ALARM_GAME_GO_INTO_THE_LIGHT -> builder.setAllowedGames(i, AlarmGameProto.ALARM_GAME_GO_INTO_THE_LIGHT)
                AlarmGame.ALARM_GAME_JUMPING_JACKS -> builder.setAllowedGames(i, AlarmGameProto.ALARM_GAME_JUMPING_JACKS)
            }
        }

        return builder.build()
    }
}
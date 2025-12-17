package ee.ut.cs.alarm.data

import ee.ut.cs.alarm.R
import java.util.UUID
import ee.ut.cs.alarm.data.proto.AlarmGame as AlarmGameProto
import ee.ut.cs.alarm.data.proto.AlarmTrack as AlarmTrackProto
import ee.ut.cs.alarm.data.proto.UserPrefs as UserPrefsProto

enum class AlarmTrack(
    val value: String,
    val alarmRes: Int,
) {
    ALARM_TRACK_1("Ringtone 1", R.raw.alarm_1),
    ALARM_TRACK_2("Ringtone 2", R.raw.alarm_2),
    ALARM_TRACK_3("Ringtone 3", R.raw.alarm_3),
    ALARM_TRACK_4("Ringtone 4", R.raw.alarm_4),
    ALARM_TRACK_5("Ringtone 5", R.raw.alarm_5),
    ALARM_TRACK_6("Ringtone 6", R.raw.alarm_6),
    ALARM_TRACK_7("Ringtone 7", R.raw.alarm_7),
    ALARM_TRACK_8("Ringtone 8", R.raw.alarm_8),
    ALARM_TRACK_9("Ringtone 9", R.raw.alarm_9),
    ALARM_TRACK_10("Ringtone 10", R.raw.alarm_10),
    ALARM_TRACK_11("Ringtone 11", R.raw.alarm_11),
}

enum class AlarmGame(
    val value: String,
) {
    ALARM_GAME_BALANCE_HOLE("Balance Hole"),
    ALARM_GAME_GO_INTO_THE_LIGHT("Go Into the Light"),
    ALARM_GAME_JUMPING_JACKS("Jumping Jacks"),
    ALARM_GAME_COLOR_POINT("Color Point"),
}

data class UserPrefs(
    val id: UUID = UUID.randomUUID(),
    val allowedTracks: List<AlarmTrack> =
        listOf(
            AlarmTrack.ALARM_TRACK_1,
            AlarmTrack.ALARM_TRACK_2,
            AlarmTrack.ALARM_TRACK_3,
            AlarmTrack.ALARM_TRACK_4,
            AlarmTrack.ALARM_TRACK_5,
            AlarmTrack.ALARM_TRACK_6,
            AlarmTrack.ALARM_TRACK_7,
            AlarmTrack.ALARM_TRACK_8,
            AlarmTrack.ALARM_TRACK_9,
            AlarmTrack.ALARM_TRACK_10,
            AlarmTrack.ALARM_TRACK_11,
        ),
    val allowedGames: List<AlarmGame> =
        listOf(
            AlarmGame.ALARM_GAME_BALANCE_HOLE,
            AlarmGame.ALARM_GAME_JUMPING_JACKS,
            AlarmGame.ALARM_GAME_GO_INTO_THE_LIGHT,
            AlarmGame.ALARM_GAME_COLOR_POINT,
        ),
    val streakCount: Int = 0,
    val prevStreakCount: Int = 0,
    val lastMinigameTs: Long = 0L,
) {
    companion object {
        fun fromProto(proto: UserPrefsProto): UserPrefs {
            if (proto.id == "") {
                return UserPrefs()
            }

            return UserPrefs(
                id = UUID.fromString(proto.id),
                allowedTracks =
                    proto.allowedTracksList.map { value ->
                        when (value) {
                            AlarmTrackProto.ALARM_TRACK_1 -> AlarmTrack.ALARM_TRACK_1
                            AlarmTrackProto.ALARM_TRACK_2 -> AlarmTrack.ALARM_TRACK_2
                            AlarmTrackProto.ALARM_TRACK_3 -> AlarmTrack.ALARM_TRACK_3
                            AlarmTrackProto.ALARM_TRACK_4 -> AlarmTrack.ALARM_TRACK_4
                            AlarmTrackProto.ALARM_TRACK_5 -> AlarmTrack.ALARM_TRACK_5
                            AlarmTrackProto.ALARM_TRACK_6 -> AlarmTrack.ALARM_TRACK_6
                            AlarmTrackProto.ALARM_TRACK_7 -> AlarmTrack.ALARM_TRACK_7
                            AlarmTrackProto.ALARM_TRACK_8 -> AlarmTrack.ALARM_TRACK_8
                            AlarmTrackProto.ALARM_TRACK_9 -> AlarmTrack.ALARM_TRACK_9
                            AlarmTrackProto.ALARM_TRACK_10 -> AlarmTrack.ALARM_TRACK_10
                            AlarmTrackProto.ALARM_TRACK_11 -> AlarmTrack.ALARM_TRACK_11
                            AlarmTrackProto.UNRECOGNIZED -> AlarmTrack.ALARM_TRACK_1
                        }
                    },
                allowedGames =
                    proto.allowedGamesList.map { value ->
                        when (value) {
                            AlarmGameProto.ALARM_GAME_BALANCE_HOLE -> AlarmGame.ALARM_GAME_BALANCE_HOLE
                            AlarmGameProto.ALARM_GAME_GO_INTO_THE_LIGHT -> AlarmGame.ALARM_GAME_GO_INTO_THE_LIGHT
                            AlarmGameProto.ALARM_GAME_JUMPING_JACKS -> AlarmGame.ALARM_GAME_JUMPING_JACKS
                            AlarmGameProto.ALARM_GAME_COLOR_POINT -> AlarmGame.ALARM_GAME_COLOR_POINT
                            AlarmGameProto.UNRECOGNIZED -> AlarmGame.ALARM_GAME_BALANCE_HOLE
                        }
                    },
                streakCount = proto.streakCount,
                prevStreakCount = proto.prevStreakCount,
                lastMinigameTs = proto.lastMinigameTs,
            )
        }
    }

    fun toProto(): UserPrefsProto {
        val builder =
            UserPrefsProto
                .newBuilder()
                .setId(id.toString())
                .setStreakCount(streakCount)
                .setPrevStreakCount(prevStreakCount)
                .setLastMinigameTs(lastMinigameTs)

        for (i in 0..<allowedTracks.size) {
            when (allowedTracks[i]) {
                AlarmTrack.ALARM_TRACK_1 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_1)
                AlarmTrack.ALARM_TRACK_2 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_2)
                AlarmTrack.ALARM_TRACK_3 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_3)
                AlarmTrack.ALARM_TRACK_4 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_4)
                AlarmTrack.ALARM_TRACK_5 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_5)
                AlarmTrack.ALARM_TRACK_6 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_6)
                AlarmTrack.ALARM_TRACK_7 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_7)
                AlarmTrack.ALARM_TRACK_8 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_8)
                AlarmTrack.ALARM_TRACK_9 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_9)
                AlarmTrack.ALARM_TRACK_10 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_10)
                AlarmTrack.ALARM_TRACK_11 -> builder.addAllowedTracks(AlarmTrackProto.ALARM_TRACK_11)
            }
        }

        for (i in 0..<allowedGames.size) {
            when (allowedGames[i]) {
                AlarmGame.ALARM_GAME_BALANCE_HOLE -> builder.addAllowedGames(AlarmGameProto.ALARM_GAME_BALANCE_HOLE)
                AlarmGame.ALARM_GAME_GO_INTO_THE_LIGHT -> builder.addAllowedGames(AlarmGameProto.ALARM_GAME_GO_INTO_THE_LIGHT)
                AlarmGame.ALARM_GAME_JUMPING_JACKS -> builder.addAllowedGames(AlarmGameProto.ALARM_GAME_JUMPING_JACKS)
                AlarmGame.ALARM_GAME_COLOR_POINT -> builder.addAllowedGames(AlarmGameProto.ALARM_GAME_COLOR_POINT)
            }
        }

        return builder.build()
    }
}

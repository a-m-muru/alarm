package ee.ut.cs.alarm.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import ee.ut.cs.alarm.data.UserPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import ee.ut.cs.alarm.data.proto.UserPrefs as UserPrefsProto

object UserPrefsSerializer : Serializer<UserPrefsProto> {
    override val defaultValue: UserPrefsProto = UserPrefsProto.getDefaultInstance();
    override suspend fun readFrom(input: InputStream): UserPrefsProto = UserPrefsProto.parseFrom(input)
    override suspend fun writeTo(t: UserPrefsProto, output: OutputStream) {
        t.writeTo(output)
    }
}


interface UserPrefsRepository {
    suspend fun getPrefs(): Flow<UserPrefs>
    suspend fun savePrefs(prefs: UserPrefs)
}

class UserPrefsRepositoryImpl private constructor(private val context: Context) : UserPrefsRepository {
    private val Context.prefsDataStore: DataStore<UserPrefsProto> by dataStore(
        fileName = "prefs.pb",
        serializer = UserPrefsSerializer
    )

    override suspend fun savePrefs(prefs: UserPrefs) {
        context.prefsDataStore.updateData { _ ->
            prefs.toProto()
        }
    }

    override suspend fun getPrefs(): Flow<UserPrefs> =
        context.prefsDataStore.data
            .map {
                UserPrefs.fromProto(it)
            }

    companion object {
        @Volatile
        private var singletonInstance: UserPrefsRepository? = null;

        fun getInstance(context: Context): UserPrefsRepository =
            singletonInstance ?: synchronized(this) {
                singletonInstance ?: UserPrefsRepositoryImpl(context.applicationContext).also {
                    singletonInstance = it
                }
            }
    }
}
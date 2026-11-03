package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CineTrackDatabase
import com.example.data.model.CustomList
import com.example.data.model.CustomListItemCrossRef
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.UserMediaState
import com.example.data.model.WatchlistType
import com.example.data.model.WatchedEpisodeHistory
import com.example.data.sample.SampleData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: CineTrackDatabase

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, CineTrackDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CineTrack", appName)
  }

  @Test
  fun `verify sample media items and streaming providers`() {
    val items = SampleData.initialMediaList
    assertTrue(items.isNotEmpty())
    val strangerThings = items.firstOrNull { it.id == "stranger_things" }
    assertNotNull(strangerThings)
    val providersAr = strangerThings!!.getProvidersForCountry("AR")
    assertTrue(providersAr.any { it.name.equals("Netflix", ignoreCase = true) })
  }

  @Test
  fun `verify custom lists and cross references in room`() = runBlocking {
    val customList = CustomList(
      id = "list_marathon",
      name = "Maratón de Finde",
      description = "Series para maratonear",
      colorHex = 0xFFFFB800
    )
    db.customListDao().insertList(customList)

    val testMedia = MediaItem(
      id = "test_serie_1",
      title = "Serie de Prueba",
      originalTitle = "Test Series",
      mediaType = MediaType.SERIE,
      year = 2024,
      posterUrl = "",
      backdropUrl = "",
      overview = "Sinopsis de prueba",
      durationText = "2 Temporadas",
      genres = "Drama",
      ageRating = "+16"
    )
    db.mediaDao().insert(testMedia)

    db.customListDao().addItemToList(
      CustomListItemCrossRef(listId = "list_marathon", mediaId = "test_serie_1")
    )

    val lists = db.customListDao().getAllLists().first()
    assertEquals(1, lists.size)
    assertEquals("Maratón de Finde", lists.first().name)

    val itemsInList = db.customListDao().getMediaForList("list_marathon").first()
    assertEquals(1, itemsInList.size)
    assertEquals("Serie de Prueba", itemsInList.first().title)
  }

  @Test
  fun `verify watched episode history persistence in room`() = runBlocking {
    val watchLog = WatchedEpisodeHistory(
      seriesId = "stranger_things",
      seasonNumber = 1,
      episodeNumber = 1,
      episodeTitle = "La desaparición de Will Byers",
      watchedTimestamp = System.currentTimeMillis(),
      rating = 4.5f,
      notes = "Capítulo excelente con gran ambientación."
    )
    val id = db.watchedEpisodeHistoryDao().insertWatchLog(watchLog)
    assertTrue(id > 0)

    val history = db.watchedEpisodeHistoryDao().getHistoryForSeries("stranger_things").first()
    assertEquals(1, history.size)
    assertEquals(1, history.first().seasonNumber)
    assertEquals(1, history.first().episodeNumber)
    assertEquals(4.5f, history.first().rating, 0.01f)
  }

  @Test
  fun `verify user progress and rewatch tracking in room`() = runBlocking {
    val state = UserMediaState(
      mediaId = "stranger_things",
      listType = WatchlistType.WATCHING,
      rating = 5.0f,
      rewatchCount = 2,
      currentSeason = 4,
      currentEpisode = 5,
      totalEpisodesWatched = 25
    )
    db.userMediaDao().insertOrUpdate(state)

    val retrieved = db.userMediaDao().getUserState("stranger_things")
    assertNotNull(retrieved)
    assertEquals(WatchlistType.WATCHING, retrieved!!.listType)
    assertEquals(4, retrieved.currentSeason)
    assertEquals(5, retrieved.currentEpisode)
    assertEquals(2, retrieved.rewatchCount)
  }
}

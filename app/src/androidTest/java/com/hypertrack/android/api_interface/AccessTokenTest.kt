package com.hypertrack.android.api_interface

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.hypertrack.android.AUTH_HEADER_KEY
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.sdk.HyperTrack
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccessTokenTest {

    companion object {
        const val PUBLISHABLE_KEY = "uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw"
        const val TAG = "AccessTokenTest"
    }

    private val context: Context?
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var hyperTrack: HyperTrack

    @Before
    fun setUp() {
        hyperTrack = HyperTrack.getInstance(context, PUBLISHABLE_KEY)
        Log.d(TAG, "HyperTrack initialized with device id ${hyperTrack.deviceID}")
    }

    @Test
    fun itShouldRequestNewTokenIfLastSavedIsNull() {
        val accessTokenRepository = AccessTokenRepository(PUBLISHABLE_KEY, hyperTrack.deviceID, null)

        val token = accessTokenRepository.getAccessToken()
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun itShouldUseLastTokenIfPresent() {

        val oldToken = "old.JWT.token"
        val accessTokenRepository = AccessTokenRepository(PUBLISHABLE_KEY, hyperTrack.deviceID, oldToken)
        val token = accessTokenRepository.getAccessToken()

        assertEquals(oldToken, token)

    }

    @Test
    fun itShouldUseRefreshLastTokenIfRequested() {

        val oldToken = "old.JWT.token"
        val accessTokenRepository = AccessTokenRepository(PUBLISHABLE_KEY, hyperTrack.deviceID, oldToken)
        val token = accessTokenRepository.refreshToken()

        assertNotEquals(oldToken, token)

    }

    @Test
    fun itShouldAddRequestTokenHeaderToRequests() {

        val lastToken = "last.JWT.token"
        val client = OkHttpClient.Builder()
            .addInterceptor(
                AccessTokenInterceptor(
                    AccessTokenRepository(PUBLISHABLE_KEY, hyperTrack.deviceID, lastToken)
                )
            )
            .build()
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(MockResponse())
        mockWebServer.start()

        client
            .newCall(Request.Builder().url(mockWebServer.url("/")).build())
            .execute()
        val recordedRequest = mockWebServer.takeRequest()

        val headers = recordedRequest.headers
        val authorizationHeader = headers[AUTH_HEADER_KEY]?:""
        assertEquals("Bearer $lastToken" , authorizationHeader)
        mockWebServer.shutdown()

    }
}
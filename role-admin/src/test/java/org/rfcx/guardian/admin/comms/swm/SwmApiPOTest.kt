package org.rfcx.guardian.admin.comms.swm

import org.junit.Test
import org.rfcx.guardian.admin.comms.swm.api.SwmApi
import org.rfcx.guardian.admin.comms.swm.api.SwmConnection
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SwmApiPOTest {

    @Test
    fun canDetectPowerOffSuccess() {
        // Arrange
        val shell = SwmMockShell(listOf("\$PO OK*3b"))
        val api = SwmApi(SwmConnection(shell))

        // Act
        val powerOffResponse = api.powerOff()

        // Assert
        assertNotNull(powerOffResponse)
        assertEquals(true, powerOffResponse)
    }

    @Test
    fun canDetectPowerOffError() {
        // Arrange
        val shell = SwmMockShell(listOf("\$PO ERR*7a"))
        val api = SwmApi(SwmConnection(shell))

        // Act
        val powerOffResponse = api.powerOff()

        // Assert
        assertNotNull(powerOffResponse)
        assertEquals(false, powerOffResponse)
    }

    @Test
    fun canDetectPowerOffNoResponse() {
        // Arrange
        val shell = SwmMockShell(listOf())
        val api = SwmApi(SwmConnection(shell))

        // Act
        val powerOffResponse = api.powerOff()

        // Assert
        assertEquals(false, powerOffResponse)
    }
}

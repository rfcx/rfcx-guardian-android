package org.rfcx.guardian.admin.comms.swm.api

interface SwmShell {
    /**
     * Send a request to the Swarm board and
     * retrieve the string response
     */
    fun execute(request: String): List<String>

}
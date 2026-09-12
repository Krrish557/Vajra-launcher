package com.vajra.launcher.data.tools

import com.vajra.launcher.models.InstallationState
import com.vajra.launcher.models.ToolDefinition

/**
 * Resolves the runtime installation status of security tools.
 * In Phase 18.6, this decouples static tool definitions from future live environment detection (Phase 18.7).
 */
class ToolStatusResolver {

    /**
     * Resolves the runtime installation state for a given tool.
     * In Phase 18.6, defaults to the tool's declared baseline state until
     * live Termux/Debian package querying is introduced in Phase 18.7.
     */
    fun resolveStatus(tool: ToolDefinition): InstallationState {
        return tool.declaredState
    }
}

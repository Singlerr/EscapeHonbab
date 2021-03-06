package io.github.eh.eh.serverside

import java.io.Serializable

enum class AgeScope(val scope: Int) : Serializable {
    SCOPE_RANDOM(0),
    SCOPE_20_25(1),
    SCOPE_26_30(2),
    SCOPE_31(3)
}
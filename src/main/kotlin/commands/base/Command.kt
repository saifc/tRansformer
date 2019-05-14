package commands.base

import Usage

interface Command {
    operator fun invoke(
        resources: Map<String, MutableList<Usage>>
    )
}
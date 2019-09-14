package dev.saifc.transformer.subcommands.base

import dev.saifc.transformer.data.Usage

interface Command {
    operator fun invoke(
        resources: Map<String, MutableList<Usage>>
    )
}
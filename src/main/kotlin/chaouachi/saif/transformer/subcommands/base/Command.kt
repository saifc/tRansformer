package chaouachi.saif.transformer.subcommands.base

import chaouachi.saif.transformer.data.Usage

interface Command {
    operator fun invoke(
        resources: Map<String, MutableList<Usage>>
    )
}
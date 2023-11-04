# Biome Squisher

[![Central](https://img.shields.io/badge/maven_central-blue?style=for-the-badge)](https://central.sonatype.com/search?q=dev.lukebemish.biomesquisher)

An innovative new approach to adding biomes to the vanilla Minecraft biome map, Biome Squisher is designed with the goal
of being non-destructive, meaning that vanilla biome placement, boundaries, and relative rarities are preserved.
Additionally, a secondary goal of Biome Squisher is to work well with multiple biome mods; gone are the days of each
worldgen mod present having it's own regions of the world, with only rare borders between them.

## A new solution to biome injection

Unlike other solutions to the problem of adding new biomes, Biome Squisher "squishes" the vanilla biomes (or, in fact,
previously added modded biomes) out of the way to open a "hole" in the biome space which a biome can be generated within.
This is done in a way that multiple biome injections can be deterministically layered on top of each other - ensuring that
even if multiple mods inject biomes in the same location, they are all properly "squished" and no biomes get disproportionately
shrunken or expanded.

Some things to note with this approach:

- borders between biomes are maintained
- biomes are squished in such a way that the relative area of biomes should not change - meaning that biomes uniformly become
  less common
- multiple mods can inject biomes at the same target location, and they will co-exist fine

It turns out that the process is not nearly that simple, and there's some pitfalls deal with. Biome Squisher accounts
for these edge cases and does the math necessary for such "squishing" for you, providing a nice datapack-based API to
inject extra biomes into the biome map.

## So how do I use this?

Biome Squisher is controlled entirely by datapack. First, a mod defines a series which Biome Squisher will execute on the biome space
of the relevant dimension, at `data/[namespace]/biomesquisher/series/[path].json`. They have the following structure:

* `"levels"`: a list of identifiers of any levels to apply the contained squishers to. Can contain any levels Biome Squisher can apply to - so, any using a noise biome source
* `"squishers"`: a list of identifiers of squishers to apply

Each squisher referenced by a series should be placed at `data/[namespace]/biomesquisher/squisher/[path].json`. They have the following structure:

* `"biome"`: the identifier of the biome to inject
* `"injection"`: defines where and how the biome will be injected. Has the following structure:
    * `"radius"`: how large the injection should be, with `0` being "nothing" and `1` being "the entire biome space"
    * `"temperature"`, `"humidify"`, `"continentalness"`, `"erosion"`, `"depth"`, and `"humidity"`: define the injection's behaviour on different axes. Each has a `"type"` field and takes one of the following forms:
        * `"type": "range"`: defines a set range to inject the biome in; in other words, the biome does not squish on this axis. `"continentalness"` and `"depth"` must define a range.
            * `"min"` and `"max"`: define the endpoints of the range
        * `"type": "squish"`: the biome should squish other biomes out of the way in this dimension. At least two dimensions must have squishing behaviour.
            * `"position"`: the posiiton in this axis to inject the biome at, and squish other biomes away from
            * `"degree"`: (optional, defaults to `1`) allows for non-square injections. Make this value larger or smaller than 1 to make the "hole" opened up by the squishing larger or smaller on this axis, shrinking accordingly on other axes
* `"snap"`: (optional; defaults to `true`) whether the biome injection should "snap" to a corner/edge between biomes within its radius, if one is present
* `"relative"`: (optional; defaults to the top of each dimension, in order) when two biomes attempt to inject at the same location, their relative values are queried. Takes a list of objects:
    * `"temperature"`, `"humidity"`, `"erosion"`, `"weirdness"`: one of `"start"`, `"center"`, or `"end"`. When resolving which side of an opened hole to move a biome injection to, Biome Squisher will go through the relatives one by one till
      it finds one with a non-`"center"` dimension that the squisher in the same location squishes in. This means that the list of relatives must give a non-`"center"` value to each dimension *exactly once*.

Biome Squisher applies registered `series` in alphabetical order, so that biome injection is platform-independent and deterministic. Additionally, the mod provides the `/biomesquisher dump` command, which takes the names of two axes and values
at the remaining four axes, and saves a PNG image slice through the biome space along the specified axes, which may be useful for debugging or generally inspecting the biome space.

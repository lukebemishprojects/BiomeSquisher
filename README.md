# Biome Squisher

[![Latest Version](https://img.shields.io/modrinth/v/biomesquisher?label=latest&style=for-the-badge)](https://modrinth.com/mod/biomesquisher)
[![CodeFactor](https://www.codefactor.io/repository/github/lukebemishprojects/biomesquisher/badge?style=for-the-badge)](https://www.codefactor.io/repository/github/lukebemishprojects/biomesquisher)
[![Central](https://img.shields.io/badge/maven_central-blue?style=for-the-badge)](https://central.sonatype.com/search?q=dev.lukebemish.biomesquisher)

An innovative new approach to adding biomes to the vanilla Minecraft biome map, Biome Squisher is designed with the goal
of being non-destructive, meaning that vanilla biome placement, boundaries, and relative rarities are preserved.
Additionally, a secondary goal of Biome Squisher is to work well with multiple biome mods; gone are the days of each
worldgen mod present having it's own regions of the world, with only rare borders between them.

### So, what's the issue?

In 1.18, Mojang drastically changed how Minecraft selects biomes during worldgen. The new system is extremely powerful,
and can be used to create all sorts of breathtaking worldgen, but is difficult for mods to inject their own biomes into
in a way that doesn't run into a number of issues. To select a biome at a location, the game first generates a value for
6 different noise functions:
- Temperature - differentiates "hot" and "cold" biomes
- Humidity - differentiates "wet" and "dry" biomes
- Continentalness - differentiates "land" and "ocean" biomes, and is used for large scale heightmap generation
- Erosion - differentiates mostly between various inland biomes; higher erosion areas are lower and flatter
- Depth - determined based on the block's distance from the surface. Used to differentiate between surface and cave biomes
- Weirdness - differentiates between various biomes; also affects how "folded" the terrain is

Internally, the game holds a map of a 6D unit cube (though technically the values for these noise functions can go
outside of the cube) with each biome having a "region" on it; when determining the biome at a given location, the game
looks up the generated noise functions in the "biome space" (that 6D cube) and finds the region the point falls in.

The issue? This biome space is completely filled by vanilla biomes. This makes sense for the vanilla game - what would
it mean if you selected a point with no biome at it, after all - but makes adding new biomes in a non-destructive fashion
difficult. After all, you can't just shove a biome into the space without clobbering vanilla biomes. Even if you were fine
with this, what if two mods want to inject at the same spot? The problem becomes complicated quickly.

### Prior art

This is not a new problem - since 1.18, various solutions have arisen. Some well known examples are [TerraBlender](https://github.com/Glitchfiend/TerraBlender/)
and [Blueprint](https://github.com/team-abnormals/blueprint). Both use a similar, rather clever approach to avoid the
issue: for a mod that wishes to add biomes, they create a new "layer" or "slice" - through the use of an additional source of noise -
within which the mod has complete control over biome placement - including delegating to the vanilla placement if desired.
This allows mods to add biomes without replacing vanilla biomes, and allows multiple biome mods to coexist. However, the
approach has a few notable limitations:
- biomes from different mods are much less likely to occur next to each other than biomes added by the same mod
- vanilla biomes become more rare in a non-uniform fashion. If, for instance, a mod adds a custom biome in the same location
  as the vanilla desert, the desert becomes more rare while every other biome is just as common
- for a similar reason, modded biomes can sometimes be difficult to find

### A new solution

Biome Squisher takes a different approach to the problem. Instead of adding a new layer, Biome Squisher "squishes" the
vanilla biomes (or, in fact, previously added modded biomes) out of the way to open a "hole" in the biome space which a
biome can be generated within. To demonstrate visually, the following is a slice along the temperature and humidity axes
of the vanilla biome space at fixed values in the other parameters. Each color represents a different biome:

<img alt="original biome space slice" src="/images/original.png" width="256">

And after injecting four different biomes into it:

<img alt="squished biome space slice" src="/images/squished.png" width="256">

Some things to note:
- borders between biomes are maintained
- biomes are squished in such a way that the relative area of biomes should not change - meaning that biomes uniformly become
  less common (this is somewhat difficult to see from this image alone as much of the squishing happens in dimensions you can't see)
- multiple mods can inject biomes at the same target location, and they will co-exist fine - the brown and red biomes in the center
  were injected at the same position

It turns out that the process is not nearly that simple, and there's some pitfalls deal with - for instance, correcting the scale
of the in-world noise to not get too many microbiomes, or not "squishing" along the continentalness or depth dimensions where such
squishing does not make sense. Biome Squisher accounts for these edge cases and does the math necessary for such "squishing" for you,
providing a nice datapack-based API to inject extra biomes into the biome map.

### So how do I use this?

Biome Squisher is controlled entirely by datapack. First, a mod defines a series which Biome Squisher will execute on the biome space
of the relevant dimension, at `data/[namespace]/biomesquisher/series/[path].json`. They have the following structure:

* `"levels"`: a list of identifiers of any levels to apply the contained squishers to. Can contain any levels Biome Squisher can apply to - so, any using a noise biome source
* `"squishers"`: a list of identifiers of squishers to apply

Each squisher referenced by a series should be placed at `data/[namespace]/biomesquisher/squisher/[path].json`. They have the following structure:

* `"biome"`: the identifier of the biome to inject
* `"injection"`: defines where and how the biome will be injected. Has the following structure:
  * `"radius"`: how large the injection should be, with `0` being "nothing" and `1` being "the entire biome space"
  * `"temperature"`, `"humidify"`, `"continentalness"`, `"erosion"`, `"depth"`, and `"humidity"`: define the injection's behaviour on different axes. Each has a `"type"` field and takes one of the following forms:
    * `"type": "range"`: defines a set range to inject the biome in; in other words, the biome does not squish on this axis. `"weirdness"`, `"erosion"`, `"continentalness"` and `"depth"` must define a range
      * `"min"` and `"max"`: define the endpoints of the range
    * `"type": "squish"`: the biome should squish other biomes out of the way in this dimension. `"temperature"` and `"humidity"` must have squishing behaviour
      * `"position"`: the posiiton in this axis to inject the biome at, and squish other biomes away from
      * `"degree"`: (optional, defaults to `1`) allows for non-square injections. Make this value larger or smaller than 1 to make the "hole" opened up by the squishing larger or smaller on this axis, shrinking accordingly on other axes
* `"snap"`: (optional; defaults to `true`) whether the biome injection should "snap" to a corner/edge between biomes within its radius, if one is present
* `"relative"`: (optional; defaults to the start of temperature) when two biomes attempt to inject at the same location, their relative values are queried. Has the following structure:
  * `"temperature"`, `"humidity"`: one of `"start"`, `"center"`, or `"end"`. When resolving which side of an opened hole to move a biome injection to, Biome Squisher will use this; at least one value must be non-`"center"`

Biome Squisher applies registered `series` in alphabetical order, so that biome injection is platform-independent and
deterministic. Additionally, the mod provides the `/biomesquisher dump` command, which takes the names of two axes,
values at the remaining four axes, and bounds to view within the two axes, and saves a PNG image slice through the biome
space along the specified axes, which may be useful for debugging or generally inspecting the biome space.

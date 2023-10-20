# Biome Squisher

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
issue: for a mod that wishes to add biomes, they create a new "layer" - through the use of an additional noise dimension -
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
biome can be generated within.

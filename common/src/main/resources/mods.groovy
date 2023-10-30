ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[40,)'

    license = 'BSD-3-clause'
    issueTrackerUrl = 'https://github.com/lukebemishprojects/BiomeSquisher/issues'

    mod {
        modId = this.buildProperties['modid']
        displayName = this.buildProperties['modname']
        version = this.version
        displayUrl = 'https://github.com/lukebemishprojects/BiomeSquisher'

        description = 'Squishes biomes to make room! Very WIP.'
        authors = [this.buildProperties['modauthor'] as String]

        dependencies {
            mod 'minecraft', {
                def minor = this.libs.versions.minecraft.split(/\./)[1] as int
                versionRange = "[${this.libs.versions.minecraft},1.${minor+1}.0)"
            }

            onForge {
                mod 'neoforge', {
                    versionRange = ">=${this.libs.versions.neoforge}"
                }
            }

            onFabric {
                mod 'fabric-api', {
                    versionRange = ">=${this.libs.versions.fabric.api.split(/\+/)[0]}"
                }
                mod 'fabricloader', {
                    versionRange = ">=${this.libs.versions.fabric.loader}"
                }
            }
        }

        entrypoints {
            main = [
                'dev.lukebemish.biomesquisher.impl.fabric.BiomeSquisherMod'
            ]
        }
    }
    onFabric {
        mixin = [
            'biomesquisher.mixins.json'
        ]
    }
}

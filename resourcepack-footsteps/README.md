# Footstep audio resource pack

Minecraft Java Edition 1.20.1, resource pack format 15.

Drop replacement OGG files into assets/decayinhwintah/sounds/footsteps/ with these names:
- snow1.ogg through snow4.ogg
- stone1.ogg through stone4.ogg
- dirt1.ogg through dirt4.ogg
- planks1.ogg through planks4.ogg

Place this resourcepack-footsteps folder, or a ZIP containing pack.mcmeta and assets at its root, in .minecraft/resourcepacks and enable it.

The pack replaces these vanilla step events:
- Snow: block.snow.step
- Stone: block.stone.step
- Dirt: block.gravel.step (vanilla dirt uses the gravel sound type)
- Planks and ordinary wood: block.wood.step

No replacement OGGs were included in the project, so add the files above before enabling the pack to avoid missing-sound warnings. Minecraft randomizes among the four variants for each surface.

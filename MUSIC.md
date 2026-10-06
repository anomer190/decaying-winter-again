# Adding custom music

Put each converted OGG in:
src/main/resources/assets/decayinhwintah/sounds/music/

For example, prep_theme.ogg should be declared in src/main/resources/assets/decayinhwintah/sounds.json like this:

    "music.prep_theme": {
      "sounds": [
        { "name": "decayinhwintah:music/prep_theme", "stream": true }
      ]
    }

The key music.prep_theme is the sound event ID. Add that ID to music_pools.json to put it in a playlist:

    {
      "cooldown_fade_seconds": 4.0,
      "day": ["decayinhwintah:music/prep_theme"],
      "night": []
    }

For several songs, list several IDs in the array. The director picks a random song when the phase starts, lets it finish, then picks another song from that phase's pool. Put night IDs in night; those tracks fade out when cooldown begins. The volume uses Minecraft's Music slider.

If both arrays are empty, vanilla music is left alone. When either pool has songs, vanilla background music is suppressed for the cycle.

## Playing one-shot sound effects

One-shot effects are separate from the music director. They play once at the requested volume and do not automatically fade. Add each event ID to src/main/resources/assets/decayinhwintah/sound_effects.json and declare its file in sounds.json.

Example sound_effects.json:

    {
      "effects": ["decayinhwintah:effect.radio_click"]
    }

Example sounds.json entry:

    "effect.radio_click": {
      "sounds": [
        { "name": "decayinhwintah:effects/radio_click" }
      ]
    }

Put radio_click.ogg in src/main/resources/assets/decayinhwintah/sounds/effects/. From gameplay code:

    SoundEffectPlayer.playAt(level, position,
            new ResourceLocation("decayinhwintah", "effect.radio_click"),
            SoundSource.PLAYERS, 1.0f, 1.0f);

The sound uses the selected sound category (here, Players), respects the game's volume settings, and ends naturally. Music fade settings do not apply to it. This API plays at a world position; add a fade control only for an effect that specifically needs one.

# Adding custom music

Put each converted OGG in:

`src/main/resources/assets/decayinhwintah/sounds/music/`

For example, `prep_theme.ogg` should be declared in `src/main/resources/assets/decayinhwintah/sounds.json` like this:

```json
{
  "music.prep_theme": {
    "sounds": [
      { "name": "decayinhwintah:music/prep_theme", "stream": true }
    ]
  }
}
```

The key `music.prep_theme` is the sound event ID. Add that ID to `music_pools.json` to put it in a playlist:

```json
{
  "cooldown_fade_seconds": 4.0,
  "day": ["decayinhwintah:music.prep_theme"],
  "night": []
}
```

For several songs, list several IDs in the array. The director picks a random song when the phase starts, lets it finish, then picks another song from that phase's pool. Put night IDs in `night`; those tracks fade out when cooldown begins. The volume uses Minecraft's Music slider.

If both arrays are empty, vanilla music is left alone. When either pool has songs, vanilla background music is suppressed for the cycle.

# ♻ Dynamic Block Regen

A data-driven server utility mod that lets blocks regenerate over time using customizable loot-style pools.

**Perfect for:**
- Regenerating ores or wood
- Lucky Block-like mechanic
- Renewable resource generation for servers

## Features

- 📦 Datapack-driven block pools
- 🎲 Weighted random block selection
- ⏱ Configurable regen timers (min/max)
- ✨ Wand tool for easy in-world setup
- 🧱 Custom placeholder blocks during regen
- 🔁 Automatic regen refresh on server restart

## 📁 Datapack Format

Create files in `data/dynamicblockregen/<pool>.json`

Example
```json5
// data/dynamicblockregen/basic_stone.json
{
    "config": {
      // Which block to replace the broken block while regenerating, recommended to be bedrock or barrier.
      "placeholder": "minecraft:bedrock"
    },
    "entries": [
        {
          "block": "minecraft:stone",
          "weight": 10,
          // Minimum/maximum regen time in ticks (a tick is about 1/20 seconds)
          "regen_time": [20, 40]
        },
        {
          "block": "minecraft:coal_ore",
          "weight": 3,
          "regen_time": [100, 150]
        },
        {
          "block": "minecraft:iron_ore",
          "weight": 1,
          "regen_time": [200, 300]
        }
    ]
}
```

## 🔧 Commands

`/dbr wand get <pool>`

Gives you a Regen Marker Wand for chosen pool. Right click on blocks to add or remove them from that pool.

`/dbr wand set <pool>`

Sets the wand in your hand to chosen pool.

`/dbr pools refresh`

Checks for any blocks that do not match the pool assigned to them and restarts their regeneration.

`/dbr pools restart`

Forces all blocks to regenerate.

`/dbr pools list`

Lists out all pools currently active.

`/dbr pools blocks <pool>`

Lists all block locations for the chosen pool.

`/dbr pools clear <pool>`

Clears all blocks from the chosen pool.
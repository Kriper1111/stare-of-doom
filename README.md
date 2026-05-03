# Stare of **Doom**

Do you get terribly irritated by vanilla Skeletons?
Do you wish to express your anger at them remotely, through a harsh glare?
Do you wish they would just go up in flames, expunged from the face of this dimension by the wrath of the sky?

<sup>No? Just me?</sup>

Well either way, this mod does exactly that! Stare at a Skeleton for 5 seconds - and it gets struck down by lightning, dying in the process. No experience or loot is dropped, but you get to feel slightly more at peace. Yes, this does summon an actual lightning bolt.

## Configuration

The mod is slightly configurable with the following behavior that you can change:

* Maximum distance between you and the skeleton, after which it's no longer in danger. Default is a 32 block radius.
* Staredown duration, after which the Skeleton gets shattered. Default value is 5 seconds. Effectively changed in tick increments.
* Staredown cooloff, the grace period for the Skeleton if you lose eye contact with it. Default is 0.2 seconds per tick (4 seconds each second).
* Obliteration method - lightning or a potion cloud. By default, a lightning bolt is cast at its location. However, this can pose a significant threat by charging nearby creepers, or give an unintended way to summon a lightning bolt. A potion cloud however, is purely visual. In either way, no experience or loot is dropped.

## What now?

This mod is designed to expell Skeletons specifially, however I may redo it to be more extendable.

This mod needs to be on the server side.

This mod injects into the skeleton tick, and each skeleton checks the distance to the server's players.
If you have a force-loaded chunk with a ton of skeletons, this may cause lag. Running this with 300 skeletons in a singleplayer testing client, I got TPS times of about 4.7ms. So this isn't free, but it is cheaper than the other way around.

## Modpacks?

Be my guest. No credit required.
Do note, this mod requires a mixin loader. I used UniMixins as one, and it would probably work the best with it.

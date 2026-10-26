# Advanced Baby Chicken Mod

A Fabric mod for Minecraft 26.3 that adds enhanced behaviors and mechanics to baby chickens.

## Project Structure

```
src/
├── main/java/com/advanced/baby/chicken/
│   ├── AdvancedBabyChicken.java          # Main mod entry point
│   ├── SleepingBabyChicken.java          # Interface for sleeping state
│   ├── DirtyBabyChicken.java             # Interface for dirty state
│   ├── goal/
│   │   └── BabyChickenSleepOnBedGoal.java # AI goal for bed-seeking at night
│   └── mixin/
│       └── ChickenMixin.java              # Main chicken entity modifications
│
└── client/java/com/advanced/baby/chicken/client/
    ├── AdvancedBabyChickenClient.java     # Client entry point
    ├── SleepingChickenRenderState.java    # Render state interface for sleeping
    ├── DirtyChickenRenderState.java       # Render state interface for dirty
    ├── DirtyChickenTextureGenerator.java  # Generates mud spots texture
    ├── DirtyBabyChickenLayer.java         # Render layer for dirty overlay
    ├── SleepingEyesTextureGenerator.java  # Generates sleeping eyes texture (unused)
    └── mixin/
        ├── ChickenRenderStateMixin.java   # Adds custom state to render state
        ├── ChickenRendererMixin.java      # Extracts state & adds render layers
        └── ChickenModelMixin.java         # Sleeping animation (flips upside down)
```

## Features

### 1. Sleeping Mechanic
- Baby chickens seek beds at night
- They flip upside down when sleeping on a bed
- Cannot be pushed while sleeping
- Located in: `BabyChickenSleepOnBedGoal.java`, `ChickenModelMixin.java`

### 2. Special Feeding
Baby chickens can be fed special foods (not available to adult chickens):
- Eggs
- Raw Chicken
- Cooked Chicken
- Melon Slice (watermelon)
- Potato
- Baked Potato

When fed:
- Speeds up growth
- Heals 2 hearts (4 HP)
- Plays squeak sound (`CHICKEN_AMBIENT_BABY`)

Baby chickens follow players holding these items (TemptGoal).

### 3. Dirty Mechanic
Baby chickens get dirty over time under certain conditions:

**Getting Dirty:**
| Condition | Time to Get Dirty |
|-----------|------------------|
| Walking on dirty blocks (dirt, sand, mud, gravel, etc.) | ~1.5 minutes |
| Rain + dirty blocks | ~25 seconds |
| Natural/idle (rare random chance) | ~12 minutes |

**Effects When Dirty:**
- 30% slower movement (attribute modifier)
- Only accepts baked potatoes as food (refuses other baby foods)
- Only follows players holding baked potatoes
- Visual: Brown mud spot texture overlay
- Visual: Mycelium particles floating above

**Getting Clean:**
- Walking into water
- Right-click with water bucket (consumes water, returns empty bucket)
- Plays splash sound and particles when cleaned

### Key Implementation Details

**Synced Entity Data (ChickenMixin.java):**
- `DATA_SLEEPING_ON_BED` - Boolean, synced to client
- `DATA_DIRTY` - Boolean, synced to client
- `advancedBabyChicken$dirtyTicks` - Integer, server-only counter

**Speed Modifier:**
```java
DIRTY_SPEED_MODIFIER = new AttributeModifier(
    AdvancedBabyChicken.id("dirty_slowdown"),
    -0.3,  // 30% speed reduction
    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
);
```

**Dirty Blocks:**
Dirt, Coarse Dirt, Rooted Dirt, Mud, Muddy Mangrove Roots, Sand, Red Sand, Gravel, Soul Sand, Soul Soil, Farmland

## Build & Run

```bash
# Build
./gradlew build

# Run client
./gradlew runClient

# Run server
./gradlew runServer
```

Or use the IntelliJ run configurations: "Minecraft Client" / "Minecraft Server"

## Technical Notes

- Target: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.164.0
- Java 25 compatibility
- Uses split source sets (main/client)
- Mixin compatibility level: JAVA_25

### Mixin Targets
- `ChickenMixin` targets `Chicken.class` and extends `Animal`
- Cannot inject into `mobInteract` (not defined in Chicken), must override instead
- `aiStep` injection handles both dirty tick logic and client-side particles

### Render Layer Pattern
1. Create interface for render state (e.g., `DirtyChickenRenderState`)
2. Mixin into `ChickenRenderState` to implement interface
3. Mixin into `ChickenRenderer.extractRenderState` to copy entity state to render state
4. Create `RenderLayer` subclass for visual effect
5. Inject into renderer constructor to add layer

package de.take_weiland.bedrockrefiller;

import com.google.common.primitives.Ints;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author diesieben07
 */
@Mod(modid = BedrockRefiller.MOD_ID, name = BedrockRefiller.NAME, version = BedrockRefiller.VERSION)
public class BedrockRefiller {

    static final String MOD_ID = "bedrockrefiller";
    static final String NAME = "BedrockRefiller";
    static final String VERSION = "1.0";

    private static Configuration config;
    private static int[] dimensionIDs;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        dimensionIDs = config.get(Configuration.CATEGORY_GENERAL, "dimensions", new int[] { 0 }, "Dimensions bedrock should be placed in").getIntList();
        if (config.hasChanged()) {
            config.save();
        }
    }

    private static final String DATA_ID = "diesieben.brefill";

    private final Set<Chunk> chunksToDo = new HashSet<>();

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (!event.getData().hasKey(DATA_ID)) {
            chunksToDo.add(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        if (event.getChunk().worldObj.isRemote) return;

        event.getData().setBoolean(DATA_ID, true);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Chunk chunk = event.getChunk();

        if (chunk.worldObj.isRemote || !chunksToDo.remove(chunk)) {
            return;
        }

        if (!Ints.contains(dimensionIDs, chunk.worldObj.provider.dimensionId)) {
            return;
        }

        System.out.println("Doing chunk");

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.func_150807_a(x, 0, z, Blocks.bedrock, 0);
            }
        }
    }

}

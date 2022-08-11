package michaelrunzler.fluiddynamics;

import com.mojang.logging.LogUtils;
import michaelrunzler.fluiddynamics.block.ModBlockItems;
import michaelrunzler.fluiddynamics.block.ModBlocks;
import michaelrunzler.fluiddynamics.item.ModItems;
import michaelrunzler.fluiddynamics.worldgen.OreGen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FluidDynamics.MODID)
public class FluidDynamics
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "fluiddynamics";

    public FluidDynamics()
    {
        // Register the setup and init methods for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register blocks, items, etc.
        ModItems.registerAllItems();
        ModBlocks.registerAllBlocks();

        IEventBus eb = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.items.register(eb);
        ModBlocks.blocks.register(eb);
        ModBlockItems.blockitems.register(eb);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(OreGen::onBiomeLoadingEvent);
    }

    private void init(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(OreGen::registerGeneratedFeatures);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo(MODID, "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    /**
     * Logs an event at the specified level to the Forge log, using the mod prefix ("<FD>").
     */
    public static void logModEvent(Level lvl, String data)
    {
        String cmpData = "<FD> " + data;
        switch (lvl)
        {
            case ERROR -> LOGGER.error(cmpData);
            case WARN -> LOGGER.warn(cmpData);
            case INFO -> LOGGER.info(cmpData);
            case DEBUG -> LOGGER.debug(cmpData);
            case TRACE -> LOGGER.trace(cmpData);
        }
    }
}

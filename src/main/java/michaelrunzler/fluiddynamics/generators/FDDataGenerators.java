package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.slf4j.event.Level;

@Mod.EventBusSubscriber(modid = FluidDynamics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FDDataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        if(event.includeServer()){
            gen.addProvider(new FDRecipeProvider(gen));
            //TODO add more providers
        }

        if(event.includeClient()){
            FluidDynamics.logModEvent(Level.DEBUG, "Generating data mappings...");
            gen.addProvider(new FDBlockStateProvider(gen, event.getExistingFileHelper()));
            gen.addProvider(new FDModelProvider(gen, event.getExistingFileHelper()));
            gen.addProvider(new FDEnLangProvider(gen));
        }
    }
}

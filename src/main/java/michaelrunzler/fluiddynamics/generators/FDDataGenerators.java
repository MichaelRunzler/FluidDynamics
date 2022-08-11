package michaelrunzler.fluiddynamics.generators;

import michaelrunzler.fluiddynamics.FluidDynamics;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = FluidDynamics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FDDataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        if(event.includeServer())
        {
            FDBlockTagProvider blockProvider = new FDBlockTagProvider(gen, event.getExistingFileHelper());
            gen.addProvider(blockProvider);
            gen.addProvider(new FDRecipeProvider(gen));
            gen.addProvider(new FDItemTagProvider(gen, blockProvider, event.getExistingFileHelper()));
            gen.addProvider(new FDLootTableProvider(gen));
        }

        if(event.includeClient())
        {
            gen.addProvider(new FDBlockStateProvider(gen, event.getExistingFileHelper()));
            gen.addProvider(new FDModelProvider(gen, event.getExistingFileHelper()));
            gen.addProvider(new FDEnLangProvider(gen));
        }
    }
}

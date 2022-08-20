package michaelrunzler.fluiddynamics.item;

import michaelrunzler.fluiddynamics.FluidDynamics;
import michaelrunzler.fluiddynamics.types.MaterialEnum;
import michaelrunzler.fluiddynamics.types.OreEnum;
import michaelrunzler.fluiddynamics.generators.FDEnLangProvider;
import michaelrunzler.fluiddynamics.generators.FDItemTagProvider;
import michaelrunzler.fluiddynamics.interfaces.CreativeTabs;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Tracks all registered items for the mod.
 */
public class ModItems
{
    public static final DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, FluidDynamics.MODID);
    public static final HashMap<String, RegistryObject<Item>> registeredItems = new HashMap<>();

    /**
     * Registers all items in the mod with the Item Registry.
     */
    public static void registerAllItems()
    {
        FluidDynamics.logModEvent(Level.DEBUG, "Starting item registration cycle...");

        //
        // Material listings
        //

        for(MaterialEnum type : MaterialEnum.values())
        {
            // Generate supplier and register items for ingot, nugget, and dust of this type
            String name = type.name().toLowerCase();
            Supplier<Item> itemSupplier = () -> new Item(new Item.Properties().tab(CreativeTabs.TAB_RESOURCES).rarity(Rarity.COMMON).stacksTo(64));

            RegistryObject<Item> ingot = registerItem("ingot_" + name, itemSupplier);
            RegistryObject<Item> nugget = registerItem("nugget_" + name, itemSupplier);
            RegistryObject<Item> dust = registerItem("dust_" + name, itemSupplier);
            
            // Generate armor pieces for this type
            RegistryObject<Item> armorHead = registerItem("armor_head_" + name, () -> new FDArmorItem(type, EquipmentSlot.HEAD));
            RegistryObject<Item> armorChest = registerItem("armor_chest_" + name, () -> new FDArmorItem(type, EquipmentSlot.CHEST));
            RegistryObject<Item> armorLegs = registerItem("armor_legs_" + name, () -> new FDArmorItem(type, EquipmentSlot.LEGS));
            RegistryObject<Item> armorFeet = registerItem("armor_feet_" + name, () -> new FDArmorItem(type, EquipmentSlot.FEET));

            FDEnLangProvider.addItemLangMapping(armorHead, type.englishName + " Helmet");
            FDEnLangProvider.addItemLangMapping(armorChest, type.englishName + " Chestplate");
            FDEnLangProvider.addItemLangMapping(armorLegs, type.englishName + " Greaves");
            FDEnLangProvider.addItemLangMapping(armorFeet, type.englishName + " Boots");

            // Generate tools for this type
            RegistryObject<Item> toolPick = registerItem("pickaxe_" + name, () -> new FDPickaxe(type));
            RegistryObject<Item> toolAxe = registerItem("axe_" + name, () -> new FDAxe(type));
            RegistryObject<Item> toolSpade = registerItem("spade_" + name, () -> new FDSpade(type));
            RegistryObject<Item> toolHoe = registerItem("hoe_" + name, () -> new FDHoe(type));
            RegistryObject<Item> toolSword = registerItem("sword_" + name, () -> new FDSword(type));

            FDEnLangProvider.addItemLangMapping(toolPick, type.englishName + " Pickaxe");
            FDEnLangProvider.addItemLangMapping(toolAxe, type.englishName + " Axe");
            FDEnLangProvider.addItemLangMapping(toolSpade, type.englishName + " Shovel");
            FDEnLangProvider.addItemLangMapping(toolHoe, type.englishName + " Hoe");
            FDEnLangProvider.addItemLangMapping(toolSword, type.englishName + " Sword");

            // Register language mappings
            FDEnLangProvider.addItemLangMapping(ingot, type.englishName + " Ingot");
            FDEnLangProvider.addItemLangMapping(nugget, type.englishName + " Nugget");
            FDEnLangProvider.addItemLangMapping(dust, "Powdered " + type.englishName);

            // Add beacon tags for ingots
            FDItemTagProvider.addTagMapping(ItemTags.BEACON_PAYMENT_ITEMS, ingot.getKey());
        }

        //
        // Crushed ore listings
        //

        for(OreEnum type : OreEnum.values())
        {
            // Same as for materials
            Supplier<Item> itemSupplier = () -> new Item(new Item.Properties().tab(CreativeTabs.TAB_RESOURCES).rarity(Rarity.COMMON).stacksTo(64));

            RegistryObject<Item> dust = registerItem("crushed_" + type.name().toLowerCase(), itemSupplier);
            RegistryObject<Item> pureDust = registerItem("purified_" + type.name().toLowerCase(), itemSupplier);
            FDEnLangProvider.addItemLangMapping(dust, "Decomposed " + type.englishName);
            FDEnLangProvider.addItemLangMapping(pureDust, "Purified " + type.englishName);
        }

        //
        // Vanilla-sourced material listings
        //

        Supplier<Item> vanillaSupplier = () -> new Item(new Item.Properties().stacksTo(64).tab(CreativeTabs.TAB_RESOURCES).rarity(Rarity.COMMON));

        RegistryObject<Item> goldOreDust = registerItem("crushed_gold_ore", vanillaSupplier);
        FDEnLangProvider.addItemLangMapping(goldOreDust, "Decomposed Gold Ore");

        RegistryObject<Item> pureGoldOre = registerItem("purified_gold_ore", vanillaSupplier);
        FDEnLangProvider.addItemLangMapping(pureGoldOre, "Purified Gold Ore");

        RegistryObject<Item> goldDust = registerItem("dust_gold", vanillaSupplier);
        FDEnLangProvider.addItemLangMapping(goldDust, "Powdered Gold");

        RegistryObject<Item> endstoneDust = registerItem("crushed_endstone", vanillaSupplier);
        FDEnLangProvider.addItemLangMapping(endstoneDust, "Decomposed End Stone");

        RegistryObject<Item> rareEarthNugget = registerItem("nugget_rare_earth", vanillaSupplier);
        FDEnLangProvider.addItemLangMapping(rareEarthNugget, "Rare-Earth Metal Deposit");

        //
        // Component Items
        //

        Supplier<Item> componentSupplier = () -> new Item(new Item.Properties().stacksTo(64).tab(CreativeTabs.TAB_ITEMS).rarity(Rarity.COMMON));

        RegistryObject<Item> machineFrame = registerItem("machine_frame", componentSupplier);
        FDEnLangProvider.addItemLangMapping(machineFrame, "Universal Machine Frame");

        RegistryObject<Item> powerConduit = registerItem("power_conduit", componentSupplier);
        FDEnLangProvider.addItemLangMapping(powerConduit, "Redstone-Beryllium Power Conduit");

        RegistryObject<Item> powerConverter = registerItem("power_converter", componentSupplier);
        FDEnLangProvider.addItemLangMapping(powerConverter, "Redstone-Beryllium Power Converter");

        RegistryObject<Item> redstoneDynamo = registerItem("redstone_dynamo", componentSupplier);
        FDEnLangProvider.addItemLangMapping(redstoneDynamo, "Redstone-Beryllium Energy Dynamo");

        RegistryObject<Item> actuator = registerItem("actuator", componentSupplier);
        FDEnLangProvider.addItemLangMapping(actuator, "Powered Actuation Servo");

        RegistryObject<Item> heatingElement = registerItem("heating_element", componentSupplier);
        FDEnLangProvider.addItemLangMapping(heatingElement, "Invar Heating Element");

        RegistryObject<Item> highTempHeatingElement = registerItem("high_temp_heating_element", componentSupplier);
        FDEnLangProvider.addItemLangMapping(highTempHeatingElement, "High-Temperature Tungsten Heating Element");

        RegistryObject<Item> pressureChamber = registerItem("pressure_vessel", componentSupplier);
        FDEnLangProvider.addItemLangMapping(pressureChamber, "Reinforced Pressure Vessel");

        RegistryObject<Item> electromagnet = registerItem("electromagnet", componentSupplier);
        FDEnLangProvider.addItemLangMapping(electromagnet, "Electromagnet Armature");

        RegistryObject<Item> superconductor = registerItem("superconductor", componentSupplier);
        FDEnLangProvider.addItemLangMapping(superconductor, "Superconducting Electromagnet Armature");

        RegistryObject<Item> superConduit = registerItem("super_conduit", componentSupplier);
        FDEnLangProvider.addItemLangMapping(superConduit, "Superconducting Power Conduit");

        RegistryObject<Item> heatExchanger = registerItem("heat_exchanger", componentSupplier);
        FDEnLangProvider.addItemLangMapping(heatExchanger, "Heat Exchanger");

        RegistryObject<Item> processor = registerItem("processor", componentSupplier);
        FDEnLangProvider.addItemLangMapping(processor, "Automation Processor");

        RegistryObject<Item> chemReactor = registerItem("chemical_reactor", componentSupplier);
        FDEnLangProvider.addItemLangMapping(chemReactor, "Electrocatalytic Reactor Core");

        //
        // Advanced Tools and Utility Items
        //

        RegistryObject<Item> depletedCell = registerItem("depleted_cell", componentSupplier);
        FDEnLangProvider.addItemLangMapping(depletedCell, "Depleted Redstone-Beryllium Power Cell");

        RegistryObject<Item> energyCell = registerItem("energy_cell", EnergyCell::new);
        FDEnLangProvider.addItemLangMapping(energyCell, "Redstone-Beryllium Power Cell");

        RegistryObject<Item> portableGrinder = registerItem("portable_grinder", PortableGrinder::new);
        FDEnLangProvider.addItemLangMapping(portableGrinder, "Portable Molecular Decompiler");

        RegistryObject<Item> beamEmitter = registerItem("beam_emitter", componentSupplier);
        FDEnLangProvider.addItemLangMapping(beamEmitter, "Particle Beam Emitter");

        // The uncharged version of the portable grinder, returns a charged version of itself when used in crafting
        RegistryObject<Item> unPortableGrinder = registerItem("uncharged_portable_grinder",
                () -> new Item(new Item.Properties().tab(CreativeTabs.TAB_TOOLS).rarity(Rarity.UNCOMMON).setNoRepair().stacksTo(1)
                        .craftRemainder(ModItems.registeredItems.get("portable_grinder").get())));
        FDEnLangProvider.addItemLangMapping(unPortableGrinder, "Portable Molecular Decompiler (uncharged)");

        FluidDynamics.logModEvent(Level.DEBUG, "...done.");
    }

    /**
     * Registers an item with the Item Registry with the specified ID, then adds the resulting registry entry to the registered item map.
     */
    public static RegistryObject<Item> registerItem(String id, Supplier<? extends Item> item)
    {
        RegistryObject<Item> obj = items.register(id, item);
        registeredItems.put(id, obj);
        return obj;
    }
}

package com.bespectacled.classicbeaches.surfacebuilder;

import com.bespectacled.classicbeaches.ClassicBeaches;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class SurfaceBuilders {
    public static final SurfaceBuilder<TernarySurfaceConfig> BEACH_SURFACE = 
        new BeachSurfaceBuilder(TernarySurfaceConfig.CODEC);
            
    public static final ConfiguredSurfaceBuilder<TernarySurfaceConfig> CONF_BEACH_SURFACE = 
        new ConfiguredSurfaceBuilder<TernarySurfaceConfig>(BEACH_SURFACE, SurfaceBuilder.GRASS_CONFIG);
    
    public static void register() {
        Registry.register(Registry.SURFACE_BUILDER, new Identifier(ClassicBeaches.ID, "beach"), BEACH_SURFACE);
        Registry.register(BuiltinRegistries.CONFIGURED_SURFACE_BUILDER, new Identifier(ClassicBeaches.ID, "beach"), CONF_BEACH_SURFACE);
    }
}

package com.bespectacled.classicbeaches.surfacebuilder;

import java.util.Random;
import java.util.stream.IntStream;

import com.bespectacled.classicbeaches.ClassicBeaches;
import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class BeachSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {
    private static final BlockState STONE = Blocks.STONE.getDefaultState();
    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
    
    private static final BlockState SAND = Blocks.SAND.getDefaultState();
    private static final BlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();
    
    private static final BlockState RED_SAND = Blocks.RED_SAND.getDefaultState();
    private static final BlockState RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();
    
    protected long seed;
    protected OctavePerlinNoiseSampler noise;
    
    public BeachSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
        super(codec);
    }

    @Override
    public void generate(
        Random random, 
        Chunk chunk, 
        Biome biome, 
        int x, 
        int z, 
        int height, 
        double surfaceNoise, 
        BlockState defaultBlock, 
        BlockState defaultFluid, 
        int seaLevel,
        int minimumHeight,
        long seed, 
        TernarySurfaceConfig ternarySurfaceConfig
    ) {
        double sandBeachThreshold = ClassicBeaches.CONFIG.sandBeachThreshold;
        double gravelBeachThreshold = ClassicBeaches.CONFIG.gravelBeachThreshold;
        boolean generateHighGravelBeaches = ClassicBeaches.CONFIG.generateHighGravelBeaches;
        
        int relX = x & 0xF;
        int relZ = z & 0xF;
        double eighth = 0.03125;

        double sandNoise = this.noise.sample(x * eighth, z * eighth, 0.0) * 75D + random.nextDouble();
        double gravelNoise = this.noise.sample(x * eighth, 109.0, z * eighth) * 75D + random.nextDouble();

        boolean genSand = sandNoise > sandBeachThreshold;
        boolean genGravel = gravelNoise > gravelBeachThreshold;
        
        int surfaceDepth = (int)(surfaceNoise / 3.0 + 3.0 + random.nextDouble() * 0.25);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        int flag = -1;
        
        BlockState topBlock = ternarySurfaceConfig.getTopMaterial();
        BlockState fillerBlock = ternarySurfaceConfig.getUnderMaterial();
        
        for (int y = height; y >= minimumHeight; --y) {
            pos.set(relX, y, relZ);
            BlockState thisBlock = chunk.getBlockState(pos);
            
            if (thisBlock.isAir()) {
                flag = -1;
            }
            
            else if (thisBlock.isOf(defaultBlock.getBlock())) {
                if (flag == -1) {
                    flag = surfaceDepth;
                    
                    if (surfaceDepth <= 0) {
                        topBlock = AIR;
                        fillerBlock = STONE;
                    } else if (y >= seaLevel - 4 && y <= seaLevel + 1) {
                        topBlock = ternarySurfaceConfig.getTopMaterial();
                        fillerBlock = ternarySurfaceConfig.getUnderMaterial();
                        
                        if (genGravel) {
                            topBlock = generateHighGravelBeaches ? GRAVEL : AIR;
                            fillerBlock = GRAVEL;
                        }
                        
                        if (genSand) {
                            topBlock = SAND;
                            fillerBlock = SAND;
                        }
                    }
                    
                    // Fill in air with fluid when generating gravel beaches
                    if (y < seaLevel && topBlock.isAir()) {
                        topBlock = defaultFluid;
                    }
                    
                    if (y >= seaLevel - 1) {
                        chunk.setBlockState(pos, topBlock, false);
                    } else {
                        chunk.setBlockState(pos, fillerBlock, false);
                    }
                }
                
                else if (flag > 0) {
                    --flag;
                    chunk.setBlockState(pos, fillerBlock, false);
                    
                    // Generates layer of sandstone starting at lowest block of sand, of height 1 to 4.
                    if (flag == 0 && fillerBlock.equals(SAND)) {
                        flag = random.nextInt(4);
                        fillerBlock = SANDSTONE;
                    } else if (flag == 0 && fillerBlock.equals(RED_SAND)) {
                        flag = random.nextInt(4);
                        fillerBlock = RED_SANDSTONE;
                    }
                }
            }
        }
    }
    
    @Override
    public void initSeed(long seed) {
        if (this.seed != seed || this.noise == null) {
            this.noise = new OctavePerlinNoiseSampler(new ChunkRandom(seed), IntStream.rangeClosed(-3, 0));
        }
        
        this.seed = seed;
    }
}

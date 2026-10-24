package com.nowa.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;

/**
 * Bloc de pierres polies (9 pierres d'une même classe). Sa classe de pureté est conservée quand on
 * le pose, qu'on le pousse au piston, puis qu'on le casse.
 *
 * <p>C'est un bloc tout simple, sans entité : sa pureté est notée dans le chunk, à sa position, par
 * {@link com.nowa.world.GemBlockPurity} — le même mécanisme que pour les blocs de diamant et
 * d'émeraude du jeu. Une entité de bloc l'aurait rendu impossible à pousser.
 */
public class PurityBlock extends Block {
    public static final MapCodec<PurityBlock> CODEC = createCodec(PurityBlock::new);

    public PurityBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }
}

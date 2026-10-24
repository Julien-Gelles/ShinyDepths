package com.nowa.item;

import com.nowa.item.ModJewelry.Kind;
import com.nowa.jewelry.Jewelry;
import com.nowa.jewelry.JewelryInventory;
import com.nowa.jewelry.Worn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Un bijou se met d'un clic droit, comme une pièce d'armure : il prend sa place, et celui qui
 * l'occupait revient dans la main. Même règle que le jeu : on ne remplace pas un bijou par son
 * double exact, et le créatif garde l'original en main.
 */
public class JewelryItem extends Item {
    private final Kind kind;

    public JewelryItem(Kind kind, Settings settings) {
        super(settings);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);
        Worn place = Worn.of(kind);
        JewelryInventory worn = Jewelry.of(user);
        ItemStack previous = worn.get(place);
        if (ItemStack.areEqual(held, previous)) {
            return TypedActionResult.fail(held);
        }
        if (!world.isClient()) {
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        ItemStack returned = previous.isEmpty() ? held : previous.copy();
        worn.set(place, user.isCreative() ? held.copy() : held.copyAndEmpty());
        return TypedActionResult.success(returned, world.isClient());
    }
}

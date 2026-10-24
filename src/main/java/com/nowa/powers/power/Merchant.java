package com.nowa.powers.power;

import com.nowa.powers.GemPower;
import com.nowa.powers.PowerValue;
import com.nowa.powers.Powers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.TradeOffer;

/**
 * Le marchand (émeraude) : les villageois font une remise. Elle passe par le même chemin que celle du
 * Héros du village — un rabais posé sur chaque offre quand le villageois les montre au joueur, et
 * effacé quand l'échange se ferme — et s'y ajoute.
 */
public final class Merchant {
    /** La part du prix remise. */
    private static final PowerValue DISCOUNT = PowerValue.of("merchant.discount", 0.05, 0.10, 0.15, 0.20, 0.25);

    private Merchant() {
    }

    /** Le villageois prépare ses offres pour ce joueur. Côté serveur. */
    public static void discount(PlayerEntity player, Iterable<TradeOffer> offers) {
        int level = Powers.of(player).level(GemPower.MERCHANT);
        if (level <= 0) {
            return;
        }
        double discount = DISCOUNT.at(level);
        for (TradeOffer offer : offers) {
            int off = (int) Math.round(discount * offer.getOriginalFirstBuyItem().getCount());
            if (off > 0) {
                offer.increaseSpecialPrice(-off);
            }
        }
    }
}

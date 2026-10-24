package com.nowa.headlamp;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * La lampe frontale. Elle se porte à la place du casque — c'est son prix : on troque la protection
 * contre la lumière et le sens des pierres.
 *
 * <p>C'est une pièce d'armure pour que le jeu sache tout seul la mettre sur la tête, au clic droit
 * comme dans la case du casque, et la dessiner sur le joueur. Elle ne protège de rien : on retire
 * les attributs qu'une armure s'attribue d'office, sans quoi l'infobulle annoncerait « +0 armure ».
 */
public class HeadlampItem extends ArmorItem {
    public HeadlampItem(RegistryEntry<ArmorMaterial> material, Settings settings) {
        super(material, Type.HELMET, settings);
    }

    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        return AttributeModifiersComponent.DEFAULT;
    }
}

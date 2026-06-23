package org.somethingunfunny.closest_arrow.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.neoforge.common.CommonHooks;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerMixin {

	/**
	 * @author Ranch
	 * @reason proving someone wrong
	 */
	@Overwrite
	public ItemStack getProjectile(ItemStack heldWeapon) {
		Player me = (Player) (Object) this;

		if (!(heldWeapon.getItem() instanceof ProjectileWeaponItem)) {
			return ItemStack.EMPTY;
		} else {
			Predicate<ItemStack> supportedProjectiles = ((ProjectileWeaponItem)heldWeapon.getItem()).getSupportedHeldProjectiles(heldWeapon);
			ItemStack heldProjectile = ProjectileWeaponItem.getHeldProjectile(me, supportedProjectiles);
			if (!heldProjectile.isEmpty()) {
				return CommonHooks.getProjectile(me, heldWeapon, heldProjectile);
			} else {
				supportedProjectiles = ((ProjectileWeaponItem)heldWeapon.getItem()).getAllSupportedProjectiles(heldWeapon);

				// modification starts here
				Vector2i weaponPos = new Vector2i(0, 0);
				for(int i = 0; i < me.getInventory().getContainerSize(); ++i) {
					ItemStack itemStack = me.getInventory().getItem(i);
					if (itemStack.equals(heldWeapon)) {
						weaponPos = closestArrow$getPosFromSlot(i);
						break;
					}
				}

				double closestDist = Double.MAX_VALUE;
				ItemStack closestStack = ItemStack.EMPTY;
				for(int i = 0; i < me.getInventory().getContainerSize(); ++i) {
					ItemStack itemStack = me.getInventory().getItem(i);
					if (supportedProjectiles.test(itemStack)) {
						Vector2i pos = closestArrow$getPosFromSlot(i);
						double dist = pos.distance(weaponPos);
						if (dist < closestDist) {
							closestDist = dist;
							closestStack = itemStack;
						}
					}
				}

				if (!closestStack.isEmpty()) {
					return CommonHooks.getProjectile(me, heldWeapon, closestStack);
				}
				// modification ends here

				return CommonHooks.getProjectile(me, heldWeapon, me.getAbilities().instabuild ? ((ProjectileWeaponItem)heldWeapon.getItem()).getDefaultCreativeAmmo(me, heldWeapon) : ItemStack.EMPTY);
			}
		}
	}

	// hotbar 0-8
	// inventory 9-25
	@Unique
	private Vector2i closestArrow$getPosFromSlot(int slot) {
		if (slot < 9) {
			return new Vector2i(slot, 3);
		} else {
			return new Vector2i((slot - 9) % 9, (slot - 9) / 9);
		}
	}
}

package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.registry.BGItems;
import com.direwolf20.core.items.EnergizedItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Gadget extends EnergizedItem {
    public Gadget() {
        super(new Properties().group(BGItems.ITEM_GROUP).maxStackSize(1).maxDamage(0));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tipStack, ITooltipFlag flag) {
        super.addInformation(stack, world, tipStack, flag);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if( player.isShiftKeyDown() )
            return onItemShiftRightClick(world, player, hand);

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        //TODO Remove debug code
        if (!context.getWorld().isRemote()) {
            context.getItem().getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
                energy.receiveEnergy(1000, false);
            });
            return ActionResultType.CONSUME;
        }
        return super.onItemUse(context);
    }

    public ActionResult<ItemStack> onItemShiftRightClick(World world, PlayerEntity player, Hand hand) {
        return ActionResult.resultFail(player.getHeldItem(hand));
    }

    public ItemStack get(PlayerEntity player) {
        ItemStack stack = player.getHeldItemMainhand();
        if( stack.getItem() instanceof Gadget )
            return stack;

        stack = player.getHeldItemOffhand();
        if( stack.getItem() instanceof Gadget )
            return stack;

        return ItemStack.EMPTY;
    }
}

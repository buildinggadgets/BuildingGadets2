package com.direwolf20.buildinggadgets.common.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Gadget extends Item {
    public Gadget() {
        super(new Properties().maxStackSize(1).maxDamage(0));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tipStack, ITooltipFlag flag) {
        super.addInformation(stack, world, tipStack, flag);
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if( player.isShiftKeyDown() )
            return onItemShiftRightClick(world, player, hand);

        return super.onItemRightClick(world, player, hand);
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

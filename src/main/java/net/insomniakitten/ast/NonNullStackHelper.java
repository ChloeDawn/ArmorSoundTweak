package net.insomniakitten.ast;

/*
 * This file was created at 22:46 on 05 Oct 2017 by InsomniaKitten
 *
 * It is distributed as part of the ArmorSoundTweak mod.
 * Source code is visible at: https://github.com/InsomniaKitten/ArmorSoundTweak
 *
 * Copyright (c) InsomniaKitten 2017. All Rights Reserved.
 */

import net.minecraft.item.ItemStack;

public class NonNullStackHelper {

    protected static boolean isEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

}

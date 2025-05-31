package cn.ksmcbrigade.ailocalizer.mixin;

import cn.ksmcbrigade.ailocalizer.CommonClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init",at = @At("TAIL"))
    public void init(CallbackInfo ci){
        new Thread(()->{
            if(CommonClass.r) return;
            Thread.yield();
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Minecraft.getInstance().reloadResourcePacks();
        }).start();
    }
}

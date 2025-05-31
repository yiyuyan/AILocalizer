package cn.ksmcbrigade.ailocalizer.mixin;

import cn.ksmcbrigade.ailocalizer.CommonClass;
import cn.ksmcbrigade.ailocalizer.Constants;
import cn.ksmcbrigade.ailocalizer.utils.AIUtils;
import cn.ksmcbrigade.ailocalizer.utils.DecodeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
    @Inject(method = "createReload",at = @At("TAIL"))
    public void init(Executor pBackgroundExecutor, Executor pGameExecutor, CompletableFuture<Unit> pWaitingFor, List<PackResources> pResourcePacks, CallbackInfoReturnable<ReloadInstance> cir) throws Exception {
        try {
            if(CommonClass.r) return;
            CommonClass.r = true;
            Minecraft MC = Minecraft.getInstance();
            ResourceManager resourceManager = MC.getResourceManager();
            Map<String,ArrayList<ResourceLocation>> namespaces = new HashMap();
            for (Pack pack : MC.getResourcePackRepository().getAvailablePacks()) {
                try (PackResources packResources = pack.open()){
                    for (String namespace : packResources.getNamespaces(PackType.CLIENT_RESOURCES)) {
                        if(!namespace.isEmpty() && !namespaces.containsKey(namespace)){
                            ArrayList<ResourceLocation> resources = new ArrayList<>();
                            packResources.listResources(PackType.CLIENT_RESOURCES,namespace,"lang",(resourceLocation, inputStreamIoSupplier) -> {
                                resources.add(resourceLocation);
                            });
                            namespaces.put(namespace,resources);
                        }
                    }
                }
            }
            File tmp = new File(RandomStringUtils.randomNumeric(12));

            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd 'at' HH:mm:ss z");
            for (String string : namespaces.keySet()) {
                boolean en = false,zh = false;
                for (ResourceLocation location : namespaces.get(string)) {
                    if(location.getPath().endsWith("en_us.json")&& resourceManager.getResource(location).isPresent()){
                        en = true;
                    }
                    if(location.getPath().endsWith("zh_cn.json") && resourceManager.getResource(location).isPresent()){
                        zh = true;
                    }
                }
                if(en && !zh && !string.equalsIgnoreCase("minecraft") && !string.equalsIgnoreCase("c") && !string.equalsIgnoreCase("fabric-convention-tags-v2")){
                    File assets = new File(tmp.getPath()+"/assets/"+string+"/lang");

                    File pack = tmp.toPath().resolve("pack.mcmeta").toFile();
                    File zh_cn = assets.toPath().resolve("zh_cn.json").toFile();

                    tmp.mkdir();
                    assets.mkdirs();

                    Date date = new Date(System.currentTimeMillis());
                    FileUtils.writeStringToFile(pack, """
                            {
                              "pack": {
                                "description": "Localize Resources Pack - {date}",
                                "pack_format": 48
                              }
                            }""".replace("{date}",formatter.format(date)));

                    JsonObject en_us,zh_cn_json = new JsonObject();
                    String en_us_json = null;
                    try(InputStream in = resourceManager.getResource(Objects.requireNonNull(ResourceLocation.tryBuild(string, "lang/en_us.json"))).get().open()){
                        en_us_json = new String(in.readAllBytes());
                    }
                    en_us = JsonParser.parseString(en_us_json).getAsJsonObject();

                    Constants.LOG.info("Transferring namespace: {}", string);
                    for (String s : en_us.keySet()) {
                        Constants.LOG.info("Transferring object: {}", en_us.get(s).getAsString());
                        zh_cn_json.addProperty(s, AIUtils.transfer(en_us.get(s).getAsString(), DecodeUtil.randomStrings()));
                    }

                    FileUtils.writeStringToFile(zh_cn, zh_cn_json.toString());
                }
            }
            if(tmp.exists())Files.move(tmp.toPath(),new File("resourcepacks/"+tmp.getName()).toPath());
        } catch (Exception e) {
            Constants.LOG.error("Can't create the resource pack!",e);
        }

        /*MC.reloadResourcePacks();
        for (Pack pack : MC.getResourcePackRepository().getAvailablePacks()) {
            if(pack.getDescription().getString().contains("Localize Resources Pack")){
                ArrayList<Pack> packs = new ArrayList<>();
                packs.addAll(MC.getResourcePackRepository().getSelectedPacks());
                if(!packs.contains(pack)) packs.add(pack);
            }
        }
        MC.reloadResourcePacks();*/
    }
}

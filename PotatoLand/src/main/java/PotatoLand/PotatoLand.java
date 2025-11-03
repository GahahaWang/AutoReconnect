package PotatoLand;

import PotatoLand.config.PotatoLandConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class PotatoLand implements  ClientModInitializer{

    public final static Logger logger = LoggerFactory.getLogger("AutoReconnectPotatoLand");
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> future;
    @Override
    public void onInitializeClient() {
        PotatoLandConfig.load();
        logger.info("PotatoLand mod 初始化完成");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("disconnect")
                            .executes((context)-> {
                                logger.info("執行 disconnect 命令，斷開與伺服器的連接");
                                // 在主線程中執行斷線操作
                                mc.execute(() -> {
                                    if (mc.getConnection() != null) {
                                        mc.getConnection().getConnection().disconnect(Component.literal("手動斷線"));
                                    }
                                });
                                return 1;
                            })
            );
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("autoreconnect")
                            .executes((context)-> {
                                // 在主線程中執行斷線操作
                                mc.execute(() -> {
                                    if(PotatoLandConfig.Config.enabled) {
                                        PotatoLandConfig.Config.enabled = false;
                                        if(future!= null)future.cancel(true);
                                        context.getSource().sendFeedback(Component.literal("已禁用 PotatoLand 斷線自動重連"));
                                    } else {
                                        PotatoLandConfig.Config.enabled = true;
                                        context.getSource().sendFeedback(Component.literal("已啟用 PotatoLand 斷線自動重連"));
                                    }
                                });
                                return 1;
                            })
            );
        });

    }

    private enum PlayerState {
        NONE,
        CORNER,
        LOBBY,
        SURVIVAL
    }

    private static PlayerState playerState = PlayerState.NONE;
    private static final Minecraft mc = Minecraft.getInstance();
    public static boolean survivalTransferByPlayerCommand = false;
    private static boolean justSendSurvivalTransferCommandByMod = false;
    public static boolean isJoinPotatoLandByAutoReconnect = false;
    public static boolean justConnected = false;
    private static boolean CornerToLobbyByMod = false;

    private static boolean jc_ijplbar() {
        if(justConnected) {
            justConnected = false;
            logger.info("justConnected");
            if(isJoinPotatoLandByAutoReconnect) {
                isJoinPotatoLandByAutoReconnect = false;
                logger.info("isJoinPotatoLandByAutoReconnect");
            } else return false;
        } else return false;
        return true;
    }

    private static boolean ssstcbm() {
        if(!justSendSurvivalTransferCommandByMod) return false;
        justSendSurvivalTransferCommandByMod = false;
        logger.info("justSendSurvivalTransferCommandByMod");
        return true;
    }

    private static boolean stbpc() {
        if(!survivalTransferByPlayerCommand) return false;
        survivalTransferByPlayerCommand = false;
        logger.info("survivalTransferByPlayerCommand");
        return true;
    }

    private static boolean ctlbm() {
        if(!CornerToLobbyByMod) return false;
        CornerToLobbyByMod = false;
        logger.info("CornerToLobbyByMod");
        return true;
    }

    public static void onChangeServersWithinPotatoLand() {
        if(future!= null)future.cancel(true);
        if(!PotatoLandConfig.Config.enabled) return;
        logger.info("收到 ServerLogin，開始檢查玩家狀態");
        checkPlayerStatus();

        if( !(jc_ijplbar() ^ ssstcbm() ^ stbpc() ^ ctlbm())) return;

        logger.info("EXECUTOR_SERVICE try execute");
        if(future!= null)future.cancel(true);
        future = EXECUTOR_SERVICE.scheduleAtFixedRate(()->{
            if(future!= null)future.cancel(true);
            try {
                checkPlayerStatus();
                switch (playerState) {
                    case NONE -> atNone();
                    case CORNER -> atCorner();
                    case LOBBY -> atLobby();
                    case SURVIVAL -> atSurvival();
                }
            } catch (Exception e) {
                logger.error("Schedule Executor MC error");
                logger.error(e.getMessage());
            }
        },2, 5, TimeUnit.SECONDS);
    };

    public static Consumer<String> sendCommandToServer;

    private static void cancelFuture() {
        if(future!= null)future.cancel(true);
    }

    private static void checkPlayerStatus() {
        logger.info("checkPlayerStatus");
        LocalPlayer player = mc.player;
        if (player == null) {
            playerState = PlayerState.NONE;
            return;
        }
        if(mc.getCurrentServer() == null)  {
            playerState = PlayerState.NONE;
            return;
        }
        if(!mc.getCurrentServer().ip.toLowerCase().endsWith("potatoland.cc")) {
            playerState = PlayerState.NONE;
            return;
        }
        // 遍歷玩家背包
        logger.info("checking inventory");
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.getItem() == Items.TROPICAL_FISH) {
                if (itemStack.toString().hashCode() == 2027637655) {
                    playerState = PlayerState.CORNER;
                    logger.info("玩家狀態檢測: CORNER (發現熱帶魚)");
                    return;
                }
            }
            else if (itemStack.getItem() == Items.BOOK) {
                if (itemStack.toString().hashCode() == 743725157) {
                    playerState = PlayerState.LOBBY;
                    logger.info("玩家狀態檢測: LOBBY (發現書本)");
                    return;
                }
            }
        }
        playerState = PlayerState.SURVIVAL;
        logger.info("玩家狀態檢測: ANY SURVIVAL (沒有找到特定物品)");
    }

    private static void atNone() {
        logger.info("玩家未連接至PotatoLand"); // should not happen normally
        cancelFuture();
    }

    private static void atCorner() {
        logger.info("玩家在 Corner 狀態");
        // 在這裡添加 Corner 狀態的處理邏輯
        mc.player.getInventory().setSelectedSlot(4);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        CornerToLobbyByMod = true;
    }

    private static void atLobby() {
        logger.info("玩家在 Lobby 狀態");
        // 在這裡添加 Lobby 狀態的處理邏輯
        justSendSurvivalTransferCommandByMod = true;
        if (PotatoLandConfig.Config.last_survival_command.isEmpty()) {
           logger.warn("沒有已儲存的 survival {1/2/3/vip}\n取消本次自動登入");
            cancelFuture();
        }
        sendCommandToServer.accept(PotatoLandConfig.Config.last_survival_command);
    }

    private static void atSurvival() {
        logger.info("玩家在 Survival 狀態");
        cancelFuture();
    }
}

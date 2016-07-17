package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class MultiSeichiEffect extends JavaPlugin implements Listener {

	//このクラス自身を表すインスタンス
	public static MultiSeichiEffect instance;

	private Player player;
	private Config config;
	private BukkitTask allplayertask;
	private Gacha gacha;

	private MineBlock mineblock;
	//コマンドの一覧
	private HashMap<String, TabExecutor> commands;

	public static final HashMap<ItemStack,Double> gachaitem = new HashMap<ItemStack,Double>();
	public static final HashMap<Player,MineBlock> playermap = new HashMap<Player,MineBlock>();


	@Override
	public void onEnable(){
		instance = this;

		//Configが"なかったら"コピーする
		saveDefaultConfig();

		//config.ymlを読み込む．読み出し方法はconf.getString("key")
		config = new Config(getConfig());

		//コマンドの登録
		commands = new HashMap<String, TabExecutor>();
		commands.put("gacha", new gachaCommand(this,gachaitem));
		commands.put("seichi", new seichiCommand(this));

		gacha = new Gacha(gachaitem);

		//リスナーの登録
		getServer().getPluginManager().registerEvents(this, this);

		getLogger().info("SeichiPlugin is Enabled!");

		//一定時間おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		allplayertask = new AllPlayerTaskRunnable(playermap,config).runTaskTimer(this,100,1200 * config.getNo1PlayerInterval()+1);

	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return commands.get(cmd.getName()).onCommand(sender, cmd, label, args);
	}
	@Override
	public void onDisable() {
		getLogger().info("SeichiPlugin is Disabled!");
		allplayertask.cancel();
		//itemlistを保存したい。
	}

	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		player = event.getPlayer();
		mineblock = new MineBlock(player);

		//誰がjoinしたのか取得しplayermapに格納
		playermap.put(player,mineblock);

		//1分おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		new MinuteTaskRunnable(player,config).runTaskTimer(this,0,1201);
	}

	//プレイヤーがleftした時に実行
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();

		//誰がleftしたのか取得しplayermapに格納
		playermap.remove(player);


	}

	@EventHandler
	public void onPlayerRightClickEvent(PlayerInteractEvent event){

		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack itemstack = event.getItem();
		ItemStack present;
		int amount = 0;
		Double probability;
		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			if(itemstack.getType().equals(gacha.getskull().getType())){
				//event.setUseItemInHand(Event.Result.DENY);
				amount = player.getInventory().getItemInMainHand().getAmount();
				if (amount == 1) {
					// がちゃ券を1枚使うので、プレイヤーの手を素手にする
					player.getInventory().setItemInMainHand(null);
					} else {
					// プレイヤーが持っているガチャ券を1枚減らす
					player.getInventory().getItemInMainHand().setAmount(amount - 1);
					}
				//gacha実行
				present = gacha.runGacha();

				probability = gachaitem.get(present);


				if(player.getInventory().firstEmpty()== -1){
					player.getWorld().dropItemNaturally(player.getLocation(),present);
					if(probability < 0.0001){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！！！！！Gigantic☆大当たり！");
						sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり" + ChatColor.AQUA + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
					}else if(probability < 0.001){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！！大当たり！");
						sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり" + ChatColor.DARK_BLUE + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
					}else if(probability < 0.1){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！");
					}else{
						player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！");
					}
					player.sendMessage(ChatColor.RED + "プレゼントが下に落ちました。");
				}else{
					player.getInventory().addItem(present);
					if(probability < 0.0001){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！！！！！Gigantic☆大当たり！");
						sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり" + ChatColor.AQUA + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
					}else if(probability < 0.001){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！！大当たり！");
						sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり" + ChatColor.DARK_BLUE + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
					}else if(probability < 0.1){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！");
					}else{
						player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！");
					}
				}
			}
		}
	}

}

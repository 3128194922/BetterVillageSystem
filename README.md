# BetterVillageSystem

Minecraft 1.20.1 / Forge 47.4.6 / KubeJS 2001.6.5（可选软依赖）

将原版**按村民独立存储**的村庄声望（gossip）系统**完全接管**为**全局、按玩家存储**的统一声望系统，
并把原版村庄的全部机制回传为 **KubeJS 事件** + 提供 **VillageAPI** 脚本接口，
用于在 KubeJS 中构建完全可编程的村庄系统。

## 核心特性

### 1. 统一全局声望
- 原版声望分散在每个村民自己的 gossip 容器里：A 村的恶名 B 村不知道。
- 本模组用 Mixin 完全接管 gossip 读写：声望改为**世界级 SavedData 按玩家 UUID 存储**，
  存于主世界（`data/bettervillage_reputation.dat`），任何村庄、任何维度共用一份。
- **低声望玩家进入任何村庄都是低声望**——交易价格惩罚立即生效，无需等村民"听说"。
- 声望分量与原版 GossipType 完全一致（值×权重求和，范围约 -700 ~ +925）：

| 分量 | 权重 | 上限 | 每日衰减 | 来源 |
|---|---|---|---|---|
| major_negative | -5 | 100 | 10 | 杀村民/杀铁傀儡 |
| minor_negative | -1 | 200 | 20 | 攻击村民 |
| minor_positive | 1 | 200 | 1 | 日常善行 |
| major_positive | 5 | 100 | 0 | 治愈僵尸村民 |
| trading | 1 | 25 | 2 | 与村民交易 |

- 价格折扣逻辑沿用原版（`updateSpecialPrices`），无需额外处理即可享受全局声望效果。
- 旧存档村民身上已有的 gossip 不再被读取（完全接管语义，规划时已确认）。

### 2. 铁傀儡联动（可配置）
- 原版铁傀儡只对"攻击过自己"的玩家愤怒。本模组让**自然生成的铁傀儡对声望 ≤ 阈值（默认 -100）**
  的玩家主动敌对，周期扫描（每 100 tick）+ 声望跌落时立即警觉附近傀儡。
- 玩家建造的傀儡（isPlayerCreated）不受影响；创造/旁观玩家免疫。

### 3. 全量 KubeJS 事件（server_scripts）
| 事件 | 可取消 | 说明 |
|---|---|---|
| `BetterVillageEvents.trade` | 否 | 玩家与村民/流浪商人完成交易（含 offer） |
| `BetterVillageEvents.reputationChange` | **是** | 声望写入前：可 `cancel()`、改 `gossipType`/`amount` |
| `BetterVillageEvents.golemSummon` | **是** | 村民即将召唤铁傀儡 |
| `BetterVillageEvents.villagerGift` | **是** | 村民向"村庄英雄"送礼 |
| `BetterVillageEvents.villagerHurt` | 否 | 村民受伤（取消伤害请用 KubeJS 自带 EntityEvents.hurt） |
| `BetterVillageEvents.villagerKilled` | 否 | 村民被杀 |
| `BetterVillageEvents.zombieVillagerCured` | 否 | 僵尸村民被治愈 |
| `BetterVillageEvents.golemKilled` | 否 | 铁傀儡被杀（声望惩罚默认开启，补全原版从未触发的 golem_killed） |
| `BetterVillageEvents.golemHurt` | 否 | 铁傀儡受伤（惩罚默认关闭） |
| `BetterVillageEvents.raidStart` / `raidVictory` / `raidDefeat` | 否 | 袭击开始/胜利/失败 |
| `BetterVillageEvents.villagerLevelUp` | 否 | 村民职业等级提升 |
| `BetterVillageEvents.villagerRestock` | 否 | 村民补货 |
| `BetterVillageEvents.reputationDecay` | 否 | 每日声望衰减 |

> 注意：KubeJS 2001.6.x 中监听器的返回值会被忽略，**取消事件必须在 JS 里调用 `event.cancel()`**。

### 4. VillageAPI（JS 主动操作）
```js
VillageAPI.getReputation(player)                     // 总声望（Σ 分量×权重）
VillageAPI.getReputationValue(player, 'trading')     // 单分量原始值
VillageAPI.getReputationDetail(player)               // 全部 5 个分量的 Map
VillageAPI.addReputation(player, 'minor_positive', 25)  // 走可取消事件（changeType=custom）
VillageAPI.setReputationValue(player, 'major_negative', 100)  // 直接覆写（绕过事件）
VillageAPI.clearReputation(player)
VillageAPI.alertIronGolems(player)                   // 立即让附近傀儡敌对
VillageAPI.getGolemAggroThreshold() / isGolemAggroEnabled()
VillageAPI.getOfflineReputation(server, 'uuid-str')  // 离线玩家读取
```

## 使用示例

```js
// server_scripts/village.js
BetterVillageEvents.reputationChange(event => {
    // 攻击村民不扣声望
    if (event.changeType == 'villager_hurt') event.cancel()
    // 杀村民惩罚加倍
    if (event.changeType == 'villager_killed') event.amount = 50
})

BetterVillageEvents.trade(event => {
    if (event.offer.getResult().id == 'minecraft:diamond') {
        event.player.tell('感谢购买钻石!')
    }
})

BetterVillageEvents.golemSummon(event => {
    // 低声望才允许召唤守护傀儡
    if (VillageAPI.getReputation(event.villager.getTradingPlayer()) >= 0) event.cancel()
})

BetterVillageEvents.raidVictory(event => {
    event.level.getServer().tell(Component.literal('村庄守住了!'))
})
```

## 配置（config/bettervillagesystem-common.toml）
- `iron_golem.golemAggroEnabled`（默认 true）/ `golemAggroThreshold`（-100）/ `golemAggroRange`（16）
- `iron_golem.alertGolemsOnReputationDrop`（true）
- `golem_penalty.golemKillPenaltyEnabled`（true）/ `golemKillRepAmount`（25 → -125 声望）
- `golem_penalty.golemHurtPenaltyEnabled`（false）/ `golemHurtRepAmount`（10）
- `decay.decayEnabled`（true，镜像原版每日衰减）

## 技术实现
- **Mixin**（5 个）：`VillagerMixin`（getPlayerReputation 重定向全局 / onReputationEventFrom 完全接管 /
  spawnGolemIfNeeded / increaseMerchantCareer / restock）、`IronGolemMixin`（aiStep 敌对扫描）、
  `RaidMixin` / `RaidsMixin`（袭击状态跳变）、`GiveGiftToHeroMixin`（送礼）。
  全部目标已生成 SRG refmap（`bettervillagesystem.mixin-refmap.json`）。
- **Forge 桥**：交易（Forge 原生 `TradeWithVillagerEvent`）、LivingHurt/LivingDeath 过滤村民与傀儡、
  ServerTick 驱动每日衰减。
- **KubeJS 软依赖**：`compileOnly`+`runtimeOnly` 引用 `libs/` 下官方映射 jar；桥接类经
  `kubejs.plugins.txt` 由 KubeJS 懒加载，未装 KubeJS 时统一声望与傀儡联动照常工作。
- 多见证村民的击杀声望按（源+行为者+游戏刻）去重，只应用一次（对应原版"每个见证者各自记录同量"）。
- 非玩家行为者（袭击村民的僵尸等）会回传事件但不写入玩家声望存储。

## 构建与验证
```bash
./gradlew build        # 产物 build/libs/bettervillagesystem-1.0.0.jar
./gradlew runServer    # 开发环境（libs 中的 kubejs/rhino/architectury 会随 runtimeOnly 加载）
```
开发环境冒烟测试已通过：模组加载、Mixin 应用（golemSummon/reputationChange 实际触发）、
KubeJS 插件注册、VillageAPI 绑定、Forge 桥事件（villagerHurt/villagerKilled/golemHurt/golemKilled）均验证正常。

## 目录结构
```
com.bettervillagesystem
├─ BetterVillageSystem / Config          # 入口与 COMMON 配置
├─ reputation
│  ├─ ReputationType / PlayerReputation  # 声望分量与玩家数据（原版语义镜像）
│  ├─ GlobalReputationData               # 世界级 SavedData 全局存储
│  ├─ ReputationChangeContext            # 变更上下文（事件载荷）
│  └─ ReputationManager                  # 核心管理器（读/写/去重/衰减/傀儡联动）
├─ bridge.BetterVillageHooks             # 核心↔KubeJS 桥接口（KubeJS 缺席时为 null）
├─ mixin                                 # 5 个 Mixin
├─ event                                 # Forge 事件桥
└─ kubejs                                # 事件组、EventJS 类、VillageAPI、插件入口
```

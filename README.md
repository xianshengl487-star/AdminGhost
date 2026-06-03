# AdminGhost

Minecraft 1.20.1 Forge 纯客户端 Mod — 基于反编译源码分析的漏洞利用工具。

## 功能

### 漏洞利用面板 (Right Shift)
- **服务端物品注入** — 利用 SophisticatedCore `SetGhostSlotMessage` 无验证漏洞
- **容器漏洞利用** — 39+ Mod 的 `stillValid` 绕过、容器复制
- **Mod 包注入** — Goety / Cataclysm / Enigmatic Legacy / Mine Fargo 等
- **附魔注入** — 通过 EnchantingInfuser + PuzzlesLib 漏洞
- **一键获取** — 钻石/下界合金/神装/建筑材料/红石/生物蛋/终极套装

### Hack 模块面板 (F7)
- 移动: Fly / Speed / Sprint / Step / NoSlowDown / NoClip
- 战斗: KillAura / Criticals / Reach / AutoTotem
- 渲染: ESP / Tracers / ChestESP / Nametags / FullBright
- 玩家: NoFall / AntiHunger / AutoEat / AutoFish / FastBreak / Scaffold

### 物品生成器 (面板内打开)
- 分类浏览所有物品
- 搜索 / 自定义ID / 数量选择
- 快捷生成按钮

## 使用方法

| 按键 | 功能 |
|------|------|
| Right Shift | 漏洞利用面板 |
| F7 | Hack 模块面板 |
| F6 | HUD 开关 |

### 服务端物品注入
1. 右键打开一个 Sophisticated Backpack
2. **保持背包界面打开**
3. 按 Right Shift 打开漏洞面板
4. 展开「服务端获取物品 (SetGhostSlot)」分类
5. 点击「检查状态」确认就绪
6. 点击物品按钮获取

**前置条件**: 服务端需安装 JEI/EMI/REI (CommonMessages 注册处理器)

## 技术原理

### SetGhostSlotMessage 漏洞
```
来源: sophisticatedcore/compat/recipeviewers/common/SetGhostSlotMessage.java

private static void handleMessage(SetGhostSlotMessage msg, ServerPlayer sender) {
    if (sender == null || !(sender.containerMenu instanceof StorageContainerMenuBase)) return;
    sender.containerMenu.getSlot(msg.slotNumber).set(msg.stack);  // 无验证直接设置!
}
```

### 支持的 Mod 漏洞
- Sophisticated Backpacks/Core — SetGhostSlot / TransferFullSlot
- Curios — 死亡复制 / 容器 stillValid 绕过
- Goety — 能力注入 / 药水袋 stillValid
- Cataclysm — 盔甲键注入 / 方块实体更新
- Enigmatic Legacy — 传送 / 鞘翅推进 / 磁铁
- Enigmatic Addons — 古董袋 stillValid / 横扫攻击
- Mine Fargo — 能力注入 / Terra Blade
- Carry On — 按键模拟
- Tom's Storage — 无线终端访问
- Enchanting Infuser — 附魔注入
- Baubley Heart Canisters — 活力之刃 stillValid
- Ending Library — 跨玩家背包访问
- Tinkers' Construct — Mantle tile=null 漏洞

## 构建

```bash
# 需要 JDK 17
export JAVA_HOME="/path/to/jdk17"
./gradlew build
```

输出: `build/libs/AdminGhost-1.0.0.jar`

## 环境

- Minecraft 1.20.1
- Forge 47.x
- 纯客户端 Mod (服务端无需安装)

## 免责声明

本项目仅用于安全研究和教育目的。使用者应遵守相关法律法规。

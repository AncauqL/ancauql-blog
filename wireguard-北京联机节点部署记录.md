# WireGuard 北京联机节点部署记录

本文记录一台腾讯云轻量应用服务器从首次登录到第一个 Windows 客户端接入的最小部署流程。目标是建立一个小范围朋友可用的虚拟局域网，供支持局域网或会选取虚拟网卡路径的游戏联机使用。

本文只配置虚拟网段 `10.66.0.0/24`，**不**将所有公网流量送进 VPN。

## 1. 部署参数

| 项目 | 取值 |
| --- | --- |
| 云厂商 | 腾讯云轻量应用服务器 |
| 地域 | 北京 |
| 操作系统 | Ubuntu 24.04 LTS |
| 服务器接口名 | `wg0` |
| WireGuard 端口 | `51820/UDP` |
| 虚拟网段 | `10.66.0.0/24` |
| 服务器虚拟 IP | `10.66.0.1/24` |
| 第一个 Windows 客户端 IP | `10.66.0.2/24` |

服务器公网 IP、私钥、Windows `.pem` SSH 私钥、WireGuard 客户端配置文件都不写入本文。它们应分别保存在密码管理器或受权限保护的本地目录中。

## 2. 腾讯云侧设置

创建服务器时选择：

- 北京地域。
- Ubuntu 24.04 LTS。
- 有独立公网 IPv4 的套餐。
- SSH 密钥登录优先；私钥文件只保存一份并妥善备份。

在轻量服务器的“防火墙”中至少保留以下入站规则：

| 协议 | 端口 | 来源 | 用途 |
| --- | --- | --- | --- |
| TCP | 22 | 自己当前公网 IP，或临时全部 IPv4 | SSH 管理 |
| UDP | 51820 | 全部 IPv4 地址 | WireGuard |

网站部署完成后才额外放行 `TCP 80` 和 `TCP 443`。不要因为 WireGuard 而开放所有端口。

## 3. 首次 SSH 登录

Windows Terminal 中使用创建服务器时下载的密钥连接：

```powershell
ssh -i "C:\\path\\to\\key.pem" ubuntu@服务器公网IP
```

常见提示符：

```text
ubuntu@VM-0-7-ubuntu:~$
```

`ubuntu` 是普通登录用户。修改 `/etc/` 下的系统配置或管理服务时，命令需要加 `sudo`。`/root` 是管理员用户的家目录，和整个系统根目录 `/` 不是同一个位置。

## 4. 安装基础组件

登录服务器后执行：

```bash
sudo apt update
sudo apt upgrade -y
sudo apt install -y wireguard micro
```

`micro` 是更接近现代编辑器习惯的终端编辑器：`Ctrl+S` 保存，`Ctrl+Q` 退出。后续示例使用它编辑配置。

## 5. 生成服务端密钥

进入临时 root shell，再生成密钥：

```bash
sudo -i
umask 077
wg genkey > /etc/wireguard/server.key
wg pubkey < /etc/wireguard/server.key > /etc/wireguard/server.pub
ls -la /etc/wireguard
cat /etc/wireguard/server.pub
exit
```

预期文件：

```text
/etc/wireguard/server.key  # 私钥，禁止发送或公开
/etc/wireguard/server.pub  # 公钥，允许写入客户端配置
```

`server.pub` 输出的是服务端公钥。客户端配置中的 `[Peer] PublicKey` 要填这一个公钥。

## 6. 开启 IPv4 转发

编辑转发配置：

```bash
sudo micro /etc/sysctl.d/99-wireguard-forward.conf
```

写入：

```ini
net.ipv4.ip_forward=1
```

保存后加载并验证：

```bash
sudo sysctl --system
sudo sysctl net.ipv4.ip_forward
```

最后一条应显示：

```text
net.ipv4.ip_forward = 1
```

## 7. 创建服务端配置

创建或编辑 `/etc/wireguard/wg0.conf`：

```bash
sudo micro /etc/wireguard/wg0.conf
```

写入以下内容。`服务器私钥` 从服务器本机的 `/etc/wireguard/server.key` 读取并粘贴，绝不发送到聊天、群组或仓库。

```ini
[Interface]
Address = 10.66.0.1/24
ListenPort = 51820
PrivateKey = 服务器私钥

PostUp = iptables -I FORWARD 1 -i %i -o %i -j ACCEPT
PostDown = iptables -D FORWARD -i %i -o %i -j ACCEPT
```

启动并设置开机自动启动：

```bash
sudo chmod 600 /etc/wireguard/wg0.conf
sudo systemctl enable --now wg-quick@wg0
sudo wg show
```

首次输出应至少包含：

```text
interface: wg0
private key: (hidden)
listening port: 51820
```

此时没有 `peer` 是正常的，因为客户端尚未被添加。

## 8. 创建第一个 Windows 客户端

安装 WireGuard for Windows 后，选择“添加隧道” -> “添加空隧道”。程序会自动生成客户端私钥和公钥。

编辑本地配置，保留自动生成的 `PrivateKey`，补充其余字段：

```ini
[Interface]
PrivateKey = Windows 客户端自动生成，禁止发送
Address = 10.66.0.2/24

[Peer]
PublicKey = 服务端 server.pub 的内容
Endpoint = 服务器公网IP:51820
AllowedIPs = 10.66.0.0/24
PersistentKeepalive = 25
```

说明：

- Windows 端“接口公钥”是**客户端公钥**，后续需要添加到服务器。
- Windows 端“节点公钥”是**服务端公钥**，它写在客户端的 `[Peer]` 中。
- `AllowedIPs = 10.66.0.0/24` 表示只有访问虚拟局域网的流量会进隧道；不要改成 `0.0.0.0/0`。
- 每台设备必须使用独立密钥和独立地址，不能复制同一个 `.conf` 给朋友。

保存隧道后，只记录或传递客户端的**接口公钥**；不要导出、截图或发送包含 `PrivateKey` 的配置文件。

## 9. 将第一个客户端加入服务器

在服务器编辑配置：

```bash
sudo micro /etc/wireguard/wg0.conf
```

在文件末尾追加客户端 Peer。`客户端接口公钥` 是 Windows 程序主界面显示的接口公钥。

```ini

[Peer]
PublicKey = 客户端接口公钥
AllowedIPs = 10.66.0.2/32
```

保存后重新加载服务：

```bash
sudo systemctl restart wg-quick@wg0
sudo wg show
```

服务端会显示该 Peer，但在客户端真正连接前不会出现 `latest handshake`。

## 10. 验证连接

在 Windows WireGuard 中点击“连接”，等待约十秒，然后在服务器执行：

```bash
sudo wg show
```

成功握手时，Peer 下会出现类似内容：

```text
endpoint: 客户端公网地址:随机端口
latest handshake: 几秒前
transfer: 已收数据, 已发数据
```

然后在 Windows Terminal 测试虚拟网关：

```powershell
ping 10.66.0.1
```

握手存在且 ping 可达，表示第一台设备已经成功接入。

## 11. 添加朋友

每增加一人，重复以下分配：

| 设备 | 虚拟 IP | 服务端 AllowedIPs |
| --- | --- | --- |
| 自己 | `10.66.0.2/24` | `10.66.0.2/32` |
| 朋友 A | `10.66.0.3/24` | `10.66.0.3/32` |
| 朋友 B | `10.66.0.4/24` | `10.66.0.4/32` |

朋友客户端使用同一份服务端公钥与 Endpoint，但必须各自生成私钥和客户端公钥。每次在服务端追加新的 `[Peer]` 后，执行：

```bash
sudo systemctl restart wg-quick@wg0
```

## 12. 当前验证状态与网络限制

配置完成后，服务端应监听 `UDP 51820`，腾讯云防火墙也应放行同一端口。

如果 Windows 日志持续显示：

```text
Sending handshake initiation ...
Handshake ... did not complete after 5 seconds
```

同时服务器执行：

```bash
sudo tcpdump -ni any udp port 51820
```

却始终显示 `0 packets captured`，则客户端 UDP 数据没有到达云服务器。服务端的 WireGuard Peer、路由和 ping 在这一阶段尚不是问题。

本次环境中，校园网大概率限制了对外 UDP。应先在手机热点或其他允许 UDP 的网络下验证握手；直连 WireGuard 依赖 UDP，服务器自身无法消除本地网络的 UDP 限制。请遵守所在网络的使用规则。

# 补充：本次排障记录与常见错误

## A. `ls` 看不到 WireGuard 文件

`ls` 不带路径时只查看当前目录。提示符中的 `~` 对 root 是 `/root`，而 WireGuard 文件位于 `/etc/wireguard/`。

正确检查方式：

```bash
ls -la /etc/wireguard
```

## B. `Path '/etc/wireguard' is not accessible`

使用 `ubuntu` 普通用户直接编辑 `/etc/wireguard/wg0.conf` 会没有权限。应使用：

```bash
sudo micro /etc/wireguard/wg0.conf
```

或先执行 `sudo -i` 进入 root shell。系统级文件不要在普通用户权限下强行写入。

## C. Nano 的粘贴和快捷键混乱

Nano 的 `Ctrl+U` 是内部剪贴板粘贴，不是 Windows 剪贴板。Windows Terminal 的粘贴通常是 `Ctrl+Shift+V`。为避免混淆，本文统一使用 `micro`：`Ctrl+S` 保存，`Ctrl+Q` 退出。

## D. `chmod` 或 `systemctl` 提示权限不足

普通用户运行系统命令需要 `sudo`：

```bash
sudo chmod 600 /etc/wireguard/wg0.conf
sudo systemctl enable --now wg-quick@wg0
sudo wg show
```

系统弹出的认证提示不是 WireGuard 私钥提示。不要把 `.pem` 文件或 WireGuard 私钥输入到认证框。

## E. 客户端公钥与节点公钥混淆

服务器需要的是客户端“接口”的公钥；客户端需要的是服务器“节点”的公钥。两者方向相反：

```text
客户端接口公钥 -> 服务端 [Peer] PublicKey
服务端公钥     -> 客户端 [Peer] PublicKey
```

## F. 私钥曾出现在截图或聊天

一旦 Windows 客户端 `PrivateKey`、服务端 `server.key` 或 SSH `.pem` 文件内容出现在截图、公开聊天或仓库，应当作已泄露处理：删除对应客户端隧道或重新生成服务端密钥，并更新另一端的公钥。仅有公钥或公网 IP 不属于同等级秘密。

## G. PowerShell ping 命令粘连

以下内容会被 PowerShell 视为一个不存在的主机名：

```powershell
ping 10.66.0.1ping 10.66.0.1
```

正确命令是单独一条：

```powershell
ping 10.66.0.1
```

## H. 没有握手时直接排查游戏

先看服务器的 `sudo wg show`。没有 `latest handshake` 时，不要先排查游戏、虚拟网段转发或 ping；优先检查客户端是否已激活、Endpoint 公网 IP 和端口是否一致、腾讯云 UDP 防火墙规则，以及客户端所在网络是否允许 UDP。

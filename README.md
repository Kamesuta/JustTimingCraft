# 50人"クラフト"

50人で同時に息を合わせてクラフトしないとアイテムがクラフトできないプラグインです。

## 使い方

1. クラフトを発注できる人に `justtimingcraft.craft` 権限を付けます。
2. クラフトを発注できる人がアイテムをクラフトします。
3. 5秒以内に全員がクラフトすれば10秒間クラフトが可能になります。

## 設定

[ConfigLib](https://github.com/TeamKun/ConfigLib)のコマンドで設定できます。

例: `/justtiming config modify craftAllowTime set 15` でクラフト可能時間を15秒に設定できます。

| 設定項目 | デフォルト値 | 説明 |
| --- | --- | --- |
| craftTimeLimit | 5 | クラフトの発注からこの時間以内に全員がクラフトする必要があります |
| craftAllowTime | 10 | クラフト成功後、この時間以内ならクラフトが可能になります |
| trollToDeath | false | 戦犯が爆死します |
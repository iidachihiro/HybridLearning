# HybridLearning

自分用

## リソース
### 環境1

- `TIRE5_C_v5_Traces.txt` <br>
下図の環境のトレース。[TraceGenrator](https://github.com/iidachihiro/TraceGenerator)から生成する。
- `PC_TIRE5_C_v5.csv` <br>
下図の環境を表現する確率表 <br>
[TraceGenrator](https://github.com/iidachihiro/TraceGenerator)で要求される形式に依存
- `True_Probability_TIRE5_C_v5.csv` <br>
下図の環境を表現する確率表 <br>
上記ファイルと`BaseRules.txt`から自動生成したいが、現状手作業で作成している。<br>
<img src="/resources/images/Environment_v5.png" width="50%">

### 環境2
- `TIRE5_C_v6_Traces.txt` <br>
- `True_Probability_TIRE5_C_v6.csv` <br>
<img src="/resources/images/Environment_v6.png" width="50%">

### 環境3
- `TIRE5_C_v7_Traces.txt` <br>
- `True_Probability_TIRE5_C_v7.csv` <br>
<img src="/resources/images/Environment_v7.png" width="50%">

### 環境4
- `TIRE5_C_v8_Traces.txt` <br>
- `True_Probability_TIRE5_C_v8.csv` <br>
<img src="/resources/images/Environment_v8.png" width="50%">

## 引数
- gd: 勾配効果法
- sgd: 差分学習

## Experiment1
`$ java -cp bin Main experiment1` <br>
トレースを読んで、各ルールの各ポストコンディションにおける尤度をcsv形式で出力する。
そのあとにxlsx形式に直して、グラフ出力とかする。

## Experiment2
環境変化ポイントを特定する。 <br>
`$ java -cp bin Main experiment2 mode4` <br>
直近20回分の差を保持しておいて、前半10回分と後半10回分に分けて、それぞれ分散を計算する。分散の大きい方/小さい方が10^4より大きければ、環境変化があったとみなす。 <br>
`$ java -cp bin Main experiment2 mode5` <br>
直近20回分のSGDの計算結果を保持しておいて、前半10回分と後半10回分に分けて、それぞれ分散を計算する。分散の大きい方/小さい方が10^4より大きければ、環境変化があったとみなす。<br>
`$ java -cp bin Main experiment2 mode6` <br>
直近20回分のSGDの計算結果を保持しておいて、前半10回分と後半10回分に分けて、それぞれ差の変化量の和を計算する。変化量の和の大きい方/小さい方が10より大きければ、環境変化があったとみなす。 <br>
`$ java -cp bin Main experiment2 mode7` <br>
直近20回分の差を保持しておいて、前半10回分と後半10回分に分けて、それぞれ差の変化量の和を計算する。変化量の和の大きい方/小さい方が10より大きければ、環境変化があったとみなす。 <br>
`$ java -cp bin Main experiment2 mode8` <br>
直近200回分のSGDの計算結果を保持しておいて、前半100回分と後半100回分に分けて、それぞれSGDの計算結果の平均値を計算する。

## Experiment3
トレース生成時に使用した真の確率と、GDやSGDで計算される尤度の差の平均値を出力する。

`$ java -cp bin Main experiment3` <br>
誤差平均値を出力する。`output/ErrorValues_ex3.csv`をxlsx形式に直してグラフ出力とかする。

## Experiment4
HybrindLearningの実験。環境変化ポイントが分かっている前提で、そこからGDの計算に必要な分だけのデータ量が溜まったら一度GDに切り替える。そのあとまたSGDで計算する。 <br>

`$ java -cp bin Main experiment4 0 point1 point2 ...` <br>
出力は誤差平均値。point1, point2, ...には、真の確率ファイルで設定した環境が変化するポイントのアクションセットを書く。 <br>
~~事前に実験3で生成したファイルとマージするので、まず実験3をやっておく必要がある。~~

## Experiment4-2
Experiment4を、GD, SGDそれぞれ学習率∈{0.001, 0.05, 0.01, 0.05, 0.1, 0.5}に変えて同じ作業を行う。
作業後の`resources/learning.config`の学習率は、0.1に戻るようになっている。 <br>
比較しているGD, SGDの学習率はExperiment3に準ずる(デフォルトは0.1)。

`$ java -cp bin Main experiment4-2 0 point1 point2 ...`

## Experiment4-3
Experiment4を、各ルールについて誤差を出力する。
ファイルは各ルール毎に出力する。

## Experiment5-1
HybridLearningの実験。環境変化ポイントの特定は慎重にやる必要がある。 <br>
`HybridModelUpdator.learn2`メソッドを利用しているが、要調整(12/17段階) <br>
`$ java -cp bin Main experiment5-1` <br>
`output/ex5/`ディレクトリに各ルールの事後条件の尤度推移(`ValuesEX5.csv`)と、`error`ディレクトリと`value`ディレクトリを作成する。
  - `value`: 各ルールの事後条件の尤度を出力する(`*.csv`)
  - `error`: 各ルールの事後条件の尤度と、真の確率の差を出力する(`*.csv`)

## Experiment5-2 (最終的に使ったのはこれ)
HybridLearningの実験。環境変化ポイントの特定は慎重にやる必要がある。 <br>
`HybridModelUpdator.learn3`メソッドを利用しているが、~~要調整(12/18段階)~~ <br>
現状結構良い結果が出ている。 <br>

`$ java -cp bin Main experiment5-2` <br>
上と同じ。

## pythonディレクトリ
- `python ex4.py` <br>
実験4で生成した`../ouput/ErrorValues.csv`からグラフを生成する。
- `python ex4-2.py num` <br>
実験4-2で生成した`../ouput/ErrorValues.csv`からグラフを生成する。`num`
はトレースのアクションセット数
- `python ex4-3.py num` <br>
実験4-3用
- `ex5.py num` <br>
実験5で生成した`error`、`value`ディレクトリ下のファイルから、グラフを出力する。
- `ex5_value.py num`
実験5で生成した`ValuesEX5.csv`ファイルからグラフを出力する。

# HybridLearning

自分用

## リソース
- `TIRE5_C_v5_Traces.txt` <br>
下図の環境のトレース。[TraceGenrator](https://github.com/iidachihiro/TraceGenerator)から生成する。
- `PC_TIRE5_C_v5.csv` <br>
下図の環境を表現する確率表 <br>
[TraceGenrator](https://github.com/iidachihiro/TraceGenerator)で要求される形式に依存
- `True_Probability_TIRE5_C_v5.csv`
下図の環境を表現する確率表 <br>
上記ファイルと`BaseRules.txt`から自動生成したいが、現状手作業で作成している。
![表されている環境](/resources/images/EnvironmentalChangesSample.png)

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
直近20回分の差を保持しておいて、前半10回分と後半10回分に分けて、それぞれ差の変化量の和を計算する。変化量の和の大きい方/小さい方が10より大きければ、環境変化があったとみなす。 <br>
`$ java -cp bin Main experiment2 mode7` <br>
直近20回分のSGDの計算結果を保持しておいて、前半10回分と後半10回分に分けて、それぞれ差の変化量の和を計算する。変化量の和の大きい方/小さい方が10より大きければ、環境変化があったとみなす。

## Experiment3
トレース生成時に使用した真の確率と、GDやSGDで計算される尤度の差の平均値を出力する。

`$ java -cp bin Main experiment3` <br>
誤差平均値を出力する。`output/ErrorValues.csv`をxlsx形式に直してグラフ出力とかする。

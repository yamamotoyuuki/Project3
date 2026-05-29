# 不具合
frond-webのPC一覧画面で一覧を押下したときに、以下のメッセージが表示され画面が表示できない。

* メッセージ内容
Name for argument of type [java.lang.Long] not specified, and parameter name information not available via reflection. Ensure that the compiler uses the '-parameters' flag.

また、一覧押下時のURLをみると、以下になっており正しいようにみえない。
http://localhost:3000/api/v1/assets/4